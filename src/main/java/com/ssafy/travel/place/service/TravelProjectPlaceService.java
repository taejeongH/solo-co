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

    public void deletePlace(Long projectId, Long placeId, Long userId) {
        // 0. 권한 확인
        checkPermission(projectId, userId);
        
        placeMapper.deletePlace(placeId, projectId);
    }

}
