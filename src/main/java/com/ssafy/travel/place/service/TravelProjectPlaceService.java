package com.ssafy.travel.place.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

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

    public void addPlace(Long projectId, String googlePlaceId) {
        // 1. googlePlaceId로 장소 정보 조회 (캐시 또는 API)
    	PlaceDto placeDetails = (PlaceDto) placeService.getPlaceBriefDetails(googlePlaceId, -1L);

        // 2. DTO → Entity 변환
        TravelProjectPlace place = new TravelProjectPlace();
        place.setProjectId(projectId);
        place.setGooglePlaceId(googlePlaceId);
        place.setPlaceName(placeDetails.getName());
        place.setPlaceAddress(placeDetails.getFormattedAddress());
        
        Map<String, Object> geometry = placeDetails.getGeometry();
        if (geometry != null) {
            place.setLatitude((Double) geometry.get("lat"));
            place.setLongitude((Double) geometry.get("lng"));
        }

        // 3. DB 저장
        placeMapper.insertPlace(place);
    }

    public void deletePlace(Long projectId, Long placeId) {
        placeMapper.deletePlace(placeId, projectId);
    }

}
