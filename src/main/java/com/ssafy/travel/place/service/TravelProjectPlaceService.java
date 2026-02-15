package com.ssafy.travel.place.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.global.util.PlaceTypeConverter;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;
import org.springframework.stereotype.Service;

import com.ssafy.global.service.S3Service;
import com.ssafy.place.dto.PlaceDto;
import com.ssafy.place.service.PlaceService;
import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;
import com.ssafy.travel.project.service.ProjectEventService;

import lombok.RequiredArgsConstructor;

import com.ssafy.travel.place.dto.ProjectPlaceListResponseDto;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelProjectPlaceService {

    private final TravelProjectPlaceMapper placeMapper;
    private final PlaceService placeService;
    private final S3Service s3Service;
    private final TravelProjectMapper projectMapper;
    private final TravelProjectMemberMapper memberMapper;
    private final TravelItineraryMapper travelItineraryMapper;
    private final ProjectEventService eventService;

    private void checkPermission(Long projectId, Long userId) {
        // 1. 프로젝트 존재 여부 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    public TravelProjectPlace addPlace(Long projectId, String googlePlaceId, Long userId, String status)
            throws IOException {
        // 0. 권한 확인
        checkPermission(projectId, userId);

        // 0.5 중복 확인
        if (placeMapper.isPlaceExist(projectId, googlePlaceId)) {
            throw new CustomException(ErrorCode.PLACE_ALREADY_EXISTS);
        }

        // 1. googlePlaceId로 장소 정보 조회 (캐시 또는 API)
        PlaceDto placeDetails = (PlaceDto) placeService.getPlaceBriefDetails(googlePlaceId, -1L);

        // 2. DTO → Entity 변환
        TravelProjectPlace place = new TravelProjectPlace();
        place.setProjectId(projectId);
        place.setGooglePlaceId(googlePlaceId);
        place.setPlaceName(placeDetails.getName());
        place.setPlaceAddress(placeDetails.getFormattedAddress());
        if (status != null)
            place.setStatus(status);

        if (placeDetails.getTypes() != null && !placeDetails.getTypes().isEmpty()) {
            place.setPlaceType(PlaceTypeConverter.translatePlaceTypeToKorean(placeDetails.getTypes()));
        }

        if (placeDetails.getPhotoUrls() != null && !placeDetails.getPhotoUrls().isEmpty()) {
            String s3Url = s3Service.uploadFromUrl(placeDetails.getPhotoUrls().get(0), "place-thumbnail");
            place.setThumbnail(s3Url);
        }

        Map<String, Object> geometry = placeDetails.getGeometry();
        if (geometry != null) {
            place.setLatitude((Double) geometry.get("lat"));
            place.setLongitude((Double) geometry.get("lng"));
        }

        // 3. DB 저장
        placeMapper.insertPlace(place);

        // 4. 알림 전송 (TEMP인 경우 제외 - AI 생성 중에는 너무 많은 알림이 갈 수 있으므로 CONFIRMED인 경우만 권장하지만,
        // 실시간 편집을 위해 일단 보냄)
        if ("CONFIRMED".equals(status)) {
            eventService.notifyProjectUpdate(projectId, "PLACE_ADDED");
        }

        return place;
    }

    public void deletePlace(Long projectId, Long placeId, Long userId) {
        // 0. 권한 확인
        checkPermission(projectId, userId);

        // 1. 경로에 포함되어 있는지 확인
        if (travelItineraryMapper.isPlaceInItinerary(placeId)) {
            throw new CustomException(ErrorCode.PLACE_IN_USE);
        }

        // 2. 장소 삭제
        placeMapper.deletePlace(placeId, projectId);

        // 3. 알림 전송
        eventService.notifyProjectUpdate(projectId, "PLACE_DELETED");
    }

    public List<ProjectPlaceListResponseDto> getPlaces(Long projectId, Long userId, String sortBy, String order) {
        // 0. 권한 확인
        checkPermission(projectId, userId);

        // 1. sortBy 파라미터 검증 (SQL Injection 방지)
        List<String> allowedSorts = List.of("createdAt", "name", "placeType");
        if (!allowedSorts.contains(sortBy)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Invalid sort by parameter");
        }

        // 2. 파라미터를 맵에 담아 매퍼로 전달
        Map<String, Object> params = Map.of(
                "projectId", projectId,
                "sortBy", sortBy,
                "order", "desc".equalsIgnoreCase(order) ? "DESC" : "ASC",
                "statusFilter", "CONFIRMED" // Only show CONFIRMED places
        );

        // 3. 장소 목록 조회
        List<TravelProjectPlace> places = placeMapper.findSortedPlacesByProjectId(params);

        // 4. Entity -> DTO 변환
        return places.stream()
                .map(place -> ProjectPlaceListResponseDto.builder()
                        .placeId(place.getPlaceId())
                        .placeName(place.getPlaceName())
                        .placeAddress(place.getPlaceAddress())
                        .latitude(place.getLatitude())
                        .longitude(place.getLongitude())
                        .googlePlaceId(place.getGooglePlaceId())
                        .thumbnail(s3Service.generatePresignedUrl(place.getThumbnail()))
                        .placeType(place.getPlaceType())
                        .createdAt(place.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

}
