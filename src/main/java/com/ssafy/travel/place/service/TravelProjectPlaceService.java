package com.ssafy.travel.place.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectPlaceService {

    private final TravelProjectPlaceMapper placeMapper;

    public void addPlace(Long projectId, TravelProjectPlace place) {
        place.setProjectId(projectId);
        placeMapper.insertPlace(place);
    }

    public void deletePlace(Long projectId, Long placeId) {
        placeMapper.deletePlace(placeId, projectId);
    }

    public List<TravelProjectPlace> getPlaces(Long projectId) {
        return placeMapper.findByProjectId(projectId);
    }
}
