package com.ssafy.travel.place.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.place.dto.TravelProjectPlaceRequestDto;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectPlaceService {

    private final TravelProjectPlaceMapper placeMapper;

    public void addPlace(Long projectId, TravelProjectPlaceRequestDto requestDto) {
        // 1. DTO → Entity 변환
        TravelProjectPlace place = new TravelProjectPlace();
        place.setProjectId(projectId);
        place.setProjectId(projectId);
        place.setPlaceName(requestDto.getPlaceName());
        place.setPlaceAddress(requestDto.getPlaceAddress());
        place.setLatitude(requestDto.getLatitude());
        place.setLongitude(requestDto.getLongitude());

        // 2. DB 저장
        placeMapper.insertPlace(place);
    }

    public void deletePlace(Long projectId, Long placeId) {
        placeMapper.deletePlace(placeId, projectId);
    }

    public List<TravelProjectPlace> getPlaces(Long projectId) {
        return placeMapper.findByProjectId(projectId);
    }
}
