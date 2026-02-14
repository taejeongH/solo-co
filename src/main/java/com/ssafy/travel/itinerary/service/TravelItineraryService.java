package com.ssafy.travel.itinerary.service;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.place.dto.PlaceSearchItemDto;
import com.ssafy.place.service.PlaceService;
import com.ssafy.redis.service.AiResultCacheService;
import com.ssafy.ai.dto.AutoGenerateResponse;
import com.ssafy.ai.service.AIService;
import com.ssafy.travel.itinerary.dto.*;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto.ItineraryPlaceDto;
import com.ssafy.travel.itinerary.mapper.TravelItineraryGroupMetaMapper;
import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.itinerary.mapper.TravelItinerarySoloMetaMapper;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;
import com.ssafy.travel.place.service.TravelProjectPlaceService;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelItineraryService {

    private final TravelProjectPlaceMapper placeMapper;
    private final TravelProjectMapper projectMapper;
    private final TravelProjectMemberMapper projectMemberMapper;
    private final AIService aiService;
    private final AiResultCacheService aiResultCacheService;
    private final TravelItineraryMapper itineraryMapper;
    private final TravelItinerarySoloMetaMapper soloMapper;
    private final TravelItineraryGroupMetaMapper groupMapper;
    private final PlaceService googlePlaceService;
    private final TravelProjectPlaceService projectPlaceService;
    private final TravelProjectPlaceMapper projectPlaceMapper;

    @Transactional // Ensure atomicity of deletions
    public void deleteItinerary(Long projectId, Long userId) {
        // 0. 권한 체크
        if (!projectMemberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 1. 프로젝트 존재 여부 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 기존 경로 데이터 삭제
        itineraryMapper.deleteByProjectId(projectId);
        soloMapper.deleteByProjectId(projectId); // Delete solo metadata if exists
        groupMapper.deleteByProjectId(projectId); // Delete group metadata if exists

        // 3. 임시 장소(TEMP)도 모두 삭제
        List<TravelProjectPlace> tempPlaces = projectPlaceMapper.findByProjectIdAndStatus(projectId, "TEMP");
        for (TravelProjectPlace place : tempPlaces) {
            projectPlaceMapper.deletePlace(place.getPlaceId(), projectId);
        }
    }

    @Transactional(readOnly = true)
    public ItineraryResponseDto getItinerary(long projectId, long userId) {
        // 0. 권한 체크
        if (!projectMemberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 1. 프로젝트 존재 여부 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 메타데이터 조회
        ItineraryMetaResponseDto metaDto = null;
        if ("PERSONAL".equals(project.getProjectType())) {
            metaDto = soloMapper.findMetaByProjectId(projectId);
        } else if ("GROUP".equals(project.getProjectType())) {
            metaDto = groupMapper.findMetaByProjectId(projectId);
        }

        // 3. 여행 장소 목록 조회
        List<ItineraryPlaceResponseDto> places = itineraryMapper.findPlacesByProjectId(projectId);

        // 4. 날짜별로 그룹화
        Map<Integer, List<ItineraryPlaceResponseDto>> daysMap = places.stream()
                .collect(Collectors.groupingBy(ItineraryPlaceResponseDto::getDay));

        // 5. 응답 DTO 생성
        List<ItineraryDayResponseDto> days = daysMap.entrySet().stream()
                .map(entry -> ItineraryDayResponseDto.builder()
                        .day(entry.getKey())
                        .places(entry.getValue())
                        .build())
                .sorted((d1, d2) -> d1.getDay().compareTo(d2.getDay()))
                .collect(Collectors.toList());

        return ItineraryResponseDto.builder()
                .meta(metaDto)
                .days(days)
                .build();
    }

    public AutoGenerateResponse autoGenerate(Long projectId, Long userId) throws IOException {

        // 0. 기존 TEMP 상태의 장소들을 모두 삭제
        List<TravelProjectPlace> tempPlacesToDelete = projectPlaceMapper.findByProjectIdAndStatus(projectId, "TEMP");
        for (TravelProjectPlace place : tempPlacesToDelete) {
            projectPlaceMapper.deletePlace(place.getPlaceId(), projectId);
        }

        // 1. 프로젝트 조회
        TravelProject project = projectMapper.findById(projectId);
        if (project == null)
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);

        // 2. 여행 일수 계산
        int tripDays = calculateTripDays(project);

        // 3. 장소 조회
        List<TravelProjectPlace> places = placeMapper.findByProjectId(projectId);

        AutoGenerateResponse response = new AutoGenerateResponse();
        String aiResultId = java.util.UUID.randomUUID().toString();
        response.setProjectId(projectId);

        Set<Long> validPlaceIds = places.stream()
                .map(TravelProjectPlace::getPlaceId)
                .collect(Collectors.toSet());

        List<? extends ItineraryCandidateResponseDto> candidates = null;
        if (project.getProjectType().equals("GROUP")) {
            candidates = aiService.generateGroupItinerary(tripDays, places);
        } else {
            candidates = aiService.generateSoloItinerary(tripDays, places);
        }
        response.setCandidates(candidates);
        aiService.validate(candidates, validPlaceIds);
        enrichPlacesFromDbAndHandleNewPlaces(candidates, projectId, userId);
        aiResultCacheService.save(aiResultId, candidates);

        response.setProjectId(projectId);
        response.setAiResultId(aiResultId);

        return response;
    }

    // ai 추천 경로에서 적용되지 않은 속성들 적용
    @Transactional
    public void enrichPlacesFromDbAndHandleNewPlaces(List<? extends ItineraryCandidateResponseDto> candidates,
            Long projectId, Long userId) throws IOException {
        // Map to track newly created places within this transaction to avoid duplicates
        java.util.Map<String, TravelProjectPlace> newlyCreatedPlaces = new java.util.HashMap<>();

        for (var candidate : candidates) {
            for (var day : candidate.getDays()) {
                // Use an iterator to safely remove items
                for (java.util.Iterator<ItineraryPlaceDto> iterator = day.getPlaces().iterator(); iterator.hasNext();) {
                    ItineraryPlaceDto place = iterator.next();
                    // 기존 장소
                    if (!place.isNewPlace()) {
                        TravelProjectPlace dbPlace = placeMapper.findByPlaceId(place.getPlaceId());
                        if (dbPlace == null) {
                            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
                        }
                        applyDbPlace(place, dbPlace);
                    }
                    // 신규 장소
                    else {
                        TravelProjectPlace tempPlace = newlyCreatedPlaces.get(place.getPlaceName());

                        if (tempPlace == null) { // Not created yet in this session
                            try {
                                tempPlace = createTempPlaceFromGoogle(place.getPlaceName(), projectId, userId);
                                newlyCreatedPlaces.put(place.getPlaceName(), tempPlace); // Track it
                            } catch (CustomException e) {
                                if (e.getErrorCode() == ErrorCode.PLACE_NOT_FOUND) {
                                    // AI가 추천한 장소를 찾을 수 없으면, 목록에서 제거
                                    System.out.println(
                                            "Skipping unsearchable place suggested by AI: " + place.getPlaceName());
                                    iterator.remove();
                                    continue; // Skip to the next place
                                } else {
                                    // 다른 예외는 다시 던짐
                                    throw e;
                                }
                            }
                        }
                        applyDbPlace(place, tempPlace);
                    }
                }
            }
        }
    }

    // dto의 일부 속성들 (ai가 생성하지 않은 속성들)을 entity의 속성으로 대체
    private void applyDbPlace(ItineraryPlaceDto dto, TravelProjectPlace entity) {
        dto.setPlaceId(entity.getPlaceId());
        dto.setPlaceName(entity.getPlaceName());
        dto.setPlaceAddress(entity.getPlaceAddress());
        dto.setPlaceType(entity.getPlaceType());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setThumbnail(entity.getThumbnail());
    }

    // 새로 추가된 장소를 db에 임시 저장
    private TravelProjectPlace createTempPlaceFromGoogle(String placeName, Long projectId, Long userId)
            throws IOException {
        List<PlaceSearchItemDto> places = googlePlaceService.searchPlaces(placeName, null, null, null).getPlaces();
        if (places.isEmpty()) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND,
                    "Google 지도에서 '" + placeName + "'에 대한 검색 결과를 찾을 수 없습니다.");
        }
        String googlePlaceId = places.get(0).getPlaceId();
        TravelProjectPlace place = projectPlaceService.addPlace(projectId, googlePlaceId, userId, "TEMP");
        return place;
    }

    // 추천한 AI 경로 중 선택된 결과 저장
    @Transactional
    public void applySelectedCandidate(Long userId, Long projectId, ItineraryApplyRequestDto request) {
        TravelProject project = projectMapper.findById(projectId);
        int tripDays = calculateTripDays(project);
        List<TravelProjectPlace> places = placeMapper.findByProjectId(projectId);

        Set<Long> validPlaceIds = places.stream()
                .map(TravelProjectPlace::getPlaceId)
                .collect(Collectors.toSet());

        // Redis에서 AI 결과 조회
        @SuppressWarnings("unchecked")
        List<ItineraryCandidateResponseDto> candidates = aiResultCacheService.get(
                request.getAiResultId(),
                List.class);
        aiService.validate(candidates, validPlaceIds);

        // 선택된 후보 찾기
        ItineraryCandidateResponseDto selected = findSelectedCandidate(candidates, request.getRouteType());

        // 기존 데이터 삭제
        itineraryMapper.deleteByProjectId(projectId);
        soloMapper.deleteByProjectId(projectId);
        groupMapper.deleteByProjectId(projectId);

        // 장소 저장
        selected.getDays().forEach(day -> {
            int order = 1;
            for (var place : day.getPlaces()) {
                itineraryMapper.insertItineraryPlace(
                        projectId,
                        day.getDay(),
                        order++,
                        place.getPlaceId());
            }
        });

        // 메타 저장 (타입별)
        if (selected instanceof SoloItineraryCandidateResponseDto solo) {
            soloMapper.insertSoloMeta(projectId, solo);
        } else if (selected instanceof GroupItineraryCandidateResponseDto group) {
            System.out.println(selected.toString());
            groupMapper.insertGroupMeta(projectId, group);
        }

        Set<Long> selectedPlaceIds = selected.getDays().stream()
                .flatMap(day -> day.getPlaces().stream())
                .map(ItineraryPlaceDto::getPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<TravelProjectPlace> tempPlaces = projectPlaceMapper.findByProjectIdAndStatus(projectId, "TEMP");

        for (TravelProjectPlace place : tempPlaces) {
            if (selectedPlaceIds.contains(place.getPlaceId())) {
                // 선택된 TEMP → CONFIRMED
                projectPlaceMapper.updateStatus(
                        place.getPlaceId(),
                        "CONFIRMED");
            } else {
                // 선택 안 된 TEMP → DELETE
                projectPlaceMapper.deletePlace(place.getPlaceId(), projectId);
            }
        }
    }

    // 선택된 여행 루트 반환
    private ItineraryCandidateResponseDto findSelectedCandidate(List<ItineraryCandidateResponseDto> candidates,
            int routeType) {
        return (ItineraryCandidateResponseDto) candidates.stream()
                .filter(c -> c.getRouteType() == routeType)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ROUTE_SELECTION));
    }

    // 여행 일수 계산
    private int calculateTripDays(TravelProject project) {
        LocalDate start = LocalDate.parse(project.getStartDate());
        LocalDate end = LocalDate.parse(project.getEndDate());
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    @Transactional
    public void updateItinerary(Long projectId, Long userId, ItineraryUpdateRequestDto updateRequest)
            throws IOException {
        // Permission check
        if (!projectMemberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        List<ItineraryItemDto> resolvedPlaces = new ArrayList<>();
        if (updateRequest.getPlaces() != null) {
            for (ItineraryItemDto placeDto : updateRequest.getPlaces()) {
                if (placeDto.getDay() == null || placeDto.getOrder() == null) {
                    throw new CustomException(ErrorCode.INVALID_REQUEST, "Day and order are required for each place.");
                }

                Long placeId = placeDto.getPlaceId();

                // If googlePlaceId is provided, it's a new or potentially existing place
                if (placeDto.getGooglePlaceId() != null) {
                    TravelProjectPlace existingPlace = projectPlaceMapper
                            .findByGooglePlaceIdAndProjectId(placeDto.getGooglePlaceId(), projectId);
                    if (existingPlace != null) {
                        placeId = existingPlace.getPlaceId();
                    } else {
                        // Add the new place to the project
                        TravelProjectPlace newPlace = projectPlaceService.addPlace(projectId,
                                placeDto.getGooglePlaceId(), userId, "CONFIRMED");
                        placeId = newPlace.getPlaceId();
                    }
                }

                if (placeId == null) {
                    throw new CustomException(ErrorCode.INVALID_REQUEST,
                            "Each place must have a valid placeId or googlePlaceId.");
                }

                // Verify the final placeId belongs to the project
                TravelProjectPlace finalPlace = projectPlaceMapper.findByPlaceIdAndProjectId(placeId, projectId);
                if (finalPlace == null) {
                    throw new CustomException(ErrorCode.PLACE_NOT_FOUND,
                            "Place with ID " + placeId + " not found in this project.");
                }

                ItineraryItemDto resolvedPlace = new ItineraryItemDto();
                resolvedPlace.setPlaceId(placeId);
                resolvedPlace.setDay(placeDto.getDay());
                resolvedPlace.setOrder(placeDto.getOrder());
                resolvedPlaces.add(resolvedPlace);
            }
        }

        // 1. Delete the existing itinerary
        itineraryMapper.deleteByProjectId(projectId);

        // 2. Insert the new itinerary
        for (ItineraryItemDto resolvedPlace : resolvedPlaces) {
            itineraryMapper.insertItineraryPlace(projectId, resolvedPlace.getDay(), resolvedPlace.getOrder(),
                    resolvedPlace.getPlaceId());
        }
    }
}
