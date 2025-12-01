package com.ssafy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.entity.TravelProjectPlace;
import com.ssafy.mapper.TravelItineraryMapper;
import com.ssafy.mapper.TravelProjectPlaceMapper;

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
