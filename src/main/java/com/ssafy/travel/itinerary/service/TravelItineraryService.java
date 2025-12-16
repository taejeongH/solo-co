package com.ssafy.travel.itinerary.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.place.dto.PlaceSearchItemDto;
import com.ssafy.place.service.PlaceService;
import com.ssafy.redis.service.AiResultCacheService;
import com.ssafy.travel.ai.dto.AutoGenerateResponse;
import com.ssafy.travel.ai.service.AIService;
import com.ssafy.travel.itinerary.dto.GroupItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryApplyRequestDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto.ItineraryPlaceDto;
import com.ssafy.travel.itinerary.dto.SoloItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.mapper.TravelItineraryGroupMetaMapper;
import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.itinerary.mapper.TravelItinerarySoloMetaMapper;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;
import com.ssafy.travel.place.service.TravelProjectPlaceService;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelItineraryService {

    private final TravelProjectPlaceMapper placeMapper;
	private final TravelProjectMapper projectMapper;
    private final AIService aiService;
    private final AiResultCacheService aiResultCacheService;
    private final TravelItineraryMapper itineraryMapper;
    private final TravelItinerarySoloMetaMapper soloMapper;
    private final TravelItineraryGroupMetaMapper groupMapper;
    private final PlaceService googlePlaceService;
    private final TravelProjectPlaceService projectPlaceService;
    private final TravelProjectPlaceMapper projectPlaceMapper;
    

    public AutoGenerateResponse autoGenerate(Long projectId, Long userId) throws IOException {

        // 1. 프로젝트 조회
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);

        // 2. 여행 일수 계산
        int tripDays = calculateTripDays(project);

        // 3. 장소 조회
        List<TravelProjectPlace> places = placeMapper.findByProjectId(projectId);
        if (places.isEmpty()) {
        	throw new CustomException(ErrorCode.PLACE_REQUIRED_FOR_AI);
        }
        
        AutoGenerateResponse response = new AutoGenerateResponse();
        String aiResultId = java.util.UUID.randomUUID().toString();
        response.setProjectId(projectId);
        
        Set<Long> validPlaceIds = places.stream()
                .map(TravelProjectPlace::getPlaceId)
                .collect(Collectors.toSet());
        

        
        List<? extends ItineraryCandidateResponseDto> candidates = null;
        if(project.getProjectType().equals("GROUP")) {
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
    
  //ai 추천 경로에서 적용되지 않은 속성들 적용
    @Transactional
    public void enrichPlacesFromDbAndHandleNewPlaces(List<? extends ItineraryCandidateResponseDto> candidates,Long projectId, Long userId) throws IOException {
        for (var candidate : candidates) {
            for (var day : candidate.getDays()) {
                for (var place : day.getPlaces()) {
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
                        TravelProjectPlace tempPlace = createTempPlaceFromGoogle(place.getPlaceName(), projectId, userId);
                        applyDbPlace(place, tempPlace);
                    }
                }
            }
        }
    }
    
    //dto의 일부 속성들 (ai가 생성하지 않은 속성들)을 entity의 속성으로 대체
    private void applyDbPlace(ItineraryPlaceDto dto, TravelProjectPlace entity) {
        dto.setPlaceId(entity.getPlaceId());
        dto.setPlaceName(entity.getPlaceName());
        dto.setPlaceAddress(entity.getPlaceAddress());
        dto.setPlaceType(entity.getPlaceType());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setThumbnail(entity.getThumbnail());
    }
    
    //새로 추가된 장소를 db에 임시 저장
    private TravelProjectPlace createTempPlaceFromGoogle(String placeName, Long projectId, Long userId) throws IOException {
    	List<PlaceSearchItemDto> places = googlePlaceService.searchPlaces(placeName, null, null, null).getPlaces();
//    	throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
    	String googlePlaceId = places.get(0).getPlaceId();
    	TravelProjectPlace place = projectPlaceService.addPlace(projectId, googlePlaceId, userId, "TEMP");
        return place;
    }

    //추천한 AI 경로 중 선택된 결과 저장
    @Transactional
    public void applySelectedCandidate(Long userId, Long projectId, ItineraryApplyRequestDto request) {
    	TravelProject project = projectMapper.findById(projectId);
    	int tripDays = calculateTripDays(project);
    	List<TravelProjectPlace> places = placeMapper.findByProjectId(projectId);
    	
    	Set<Long> validPlaceIds = places.stream()
                .map(TravelProjectPlace::getPlaceId)
                .collect(Collectors.toSet());
        System.out.println(validPlaceIds.toString());
    	
    	// Redis에서 AI 결과 조회
    	@SuppressWarnings("unchecked")
        List<ItineraryCandidateResponseDto> candidates =
            aiResultCacheService.get(
                request.getAiResultId(),
                List.class
        );
    	aiService.validate(candidates, validPlaceIds);
    	

        // 선택된 후보 찾기
        ItineraryCandidateResponseDto selected =
            findSelectedCandidate(candidates, request.getRouteType());
        
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
        				place.getPlaceId()
        				);
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
        List<TravelProjectPlace> tempPlaces =
        	    projectPlaceMapper.findByProjectIdAndStatus(projectId, "TEMP");
        
        for (TravelProjectPlace place : tempPlaces) {
            if (selectedPlaceIds.contains(place.getPlaceId())) {
                // 선택된 TEMP → CONFIRMED
                projectPlaceMapper.updateStatus(
                    place.getPlaceId(),
                    "CONFIRMED"
                );
            } else {
                // 선택 안 된 TEMP → DELETE
                projectPlaceMapper.deletePlace(place.getPlaceId(), projectId);
            }
        }
    }
    
    //선택된 여행 루트 반환
    @SuppressWarnings("unchecked")
    private ItineraryCandidateResponseDto findSelectedCandidate(List<ItineraryCandidateResponseDto> candidates, int routeType) {
    	return (ItineraryCandidateResponseDto) candidates.stream()
                .filter(c -> c.getRouteType() == routeType)
                .findFirst()
                .orElseThrow(() ->
                    new CustomException(ErrorCode.INVALID_ROUTE_SELECTION)
                );
    }
    
    //여행 일수 계산
    private int calculateTripDays(TravelProject project) {
        LocalDate start = LocalDate.parse(project.getStartDate());
        LocalDate end = LocalDate.parse(project.getEndDate());
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }
}
