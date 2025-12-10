package com.ssafy.travel.place.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;
import org.springframework.stereotype.Service;

import com.ssafy.global.service.S3Service;
import com.ssafy.place.dto.PlaceDto;
import com.ssafy.place.service.PlaceService;
import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.place.dto.TravelProjectPlaceRequestDto;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;

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

    public void addPlace(Long projectId, String googlePlaceId, Long userId) throws IOException {
        // 0. 권한 확인
        checkPermission(projectId, userId);

        // 1. googlePlaceId로 장소 정보 조회 (캐시 또는 API)
    	PlaceDto placeDetails = (PlaceDto) placeService.getPlaceBriefDetails(googlePlaceId, -1L);

        // 2. DTO → Entity 변환
        TravelProjectPlace place = new TravelProjectPlace();
        place.setProjectId(projectId);
        place.setGooglePlaceId(googlePlaceId);
        place.setPlaceName(placeDetails.getName());
        place.setPlaceAddress(placeDetails.getFormattedAddress());

        if (placeDetails.getTypes() != null && !placeDetails.getTypes().isEmpty()) {
            place.setPlaceType(translatePlaceTypeToKorean(placeDetails.getTypes()));
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
    }

    private String translatePlaceTypeToKorean(List<String> types) {
        // 우선순위를 정의합니다. (더 구체적인 유형이 먼저 오도록)
        List<String> priorityOrder = List.of(
            "restaurant", "cafe", "lodging", "bank", "atm", "store",
            "bakery", "convenience_store", "pharmacy", "hospital",
            "movie_theater", "museum", "art_gallery", "library",
            "tourist_attraction", "park", "subway_station", "bus_station", "airport",
            "department_store", "shopping_mall", "bar",
            "point_of_interest", "establishment" // 덜 구체적인 유형은 뒤로
        );

        Map<String, String> typeMap = Map.ofEntries(
            Map.entry("restaurant", "음식점"),
            Map.entry("cafe", "카페"),
            Map.entry("bar", "바"),
            Map.entry("lodging", "숙소"),
            Map.entry("bank", "은행"),
            Map.entry("atm", "ATM"),
            Map.entry("store", "상점"),
            Map.entry("tourist_attraction", "관광 명소"),
            Map.entry("park", "공원"),
            Map.entry("subway_station", "지하철역"),
            Map.entry("bus_station", "버스 정류장"),
            Map.entry("airport", "공항"),
            Map.entry("department_store", "백화점"),
            Map.entry("shopping_mall", "쇼핑몰"),
            Map.entry("bakery", "베이커리"),
            Map.entry("convenience_store", "편의점"),
            Map.entry("pharmacy", "약국"),
            Map.entry("hospital", "병원"),
            Map.entry("movie_theater", "영화관"),
            Map.entry("museum", "박물관"),
            Map.entry("art_gallery", "미술관"),
            Map.entry("library", "도서관"),
            Map.entry("point_of_interest", "관심 장소"),
            Map.entry("establishment", "시설")
        );

        for (String type : priorityOrder) {
            if (types.contains(type) && typeMap.containsKey(type)) {
                return typeMap.get(type);
            }
        }

        // 우선순위 목록에 없거나 매핑되지 않은 경우, 기본값 또는 '기타' 반환
        return "기타";
    }

    private String translatePlaceTypeToKorean(String type) {
        // 이 단일 String 버전을 유지할 필요가 있다면 남겨두고,
        // 아니면 삭제하고 위 List<String> 버전만 사용합니다.
        // 현재는 addPlace에서 List<String> 버전을 사용하므로 이 버전은 사용되지 않습니다.
        // 하지만 다른 곳에서 사용될 수 있으니 일단 남겨둡니다.
        Map<String, String> typeMap = Map.ofEntries(
            Map.entry("restaurant", "음식점"),
            Map.entry("cafe", "카페"),
            Map.entry("bar", "바"),
            Map.entry("lodging", "숙소"),
            Map.entry("bank", "은행"),
            Map.entry("atm", "ATM"),
            Map.entry("store", "상점"),
            Map.entry("tourist_attraction", "관광 명소"),
            Map.entry("park", "공원"),
            Map.entry("subway_station", "지하철역"),
            Map.entry("bus_station", "버스 정류장"),
            Map.entry("airport", "공항"),
            Map.entry("department_store", "백화점"),
            Map.entry("shopping_mall", "쇼핑몰"),
            Map.entry("bakery", "베이커리"),
            Map.entry("convenience_store", "편의점"),
            Map.entry("pharmacy", "약국"),
            Map.entry("hospital", "병원"),
            Map.entry("movie_theater", "영화관"),
            Map.entry("museum", "박물관"),
            Map.entry("art_gallery", "미술관"),
            Map.entry("library", "도서관"),
            Map.entry("point_of_interest", "관심 장소"),
            Map.entry("establishment", "시설")
        );
        return typeMap.getOrDefault(type, "기타");
    }


    public void deletePlace(Long projectId, Long placeId, Long userId) {
        // 0. 권한 확인
        checkPermission(projectId, userId);
        
        placeMapper.deletePlace(placeId, projectId);
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
                "order", "desc".equalsIgnoreCase(order) ? "DESC" : "ASC"
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
                        .thumbnail(place.getThumbnail())
                        .placeType(place.getPlaceType())
                        .createdAt(place.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

}
