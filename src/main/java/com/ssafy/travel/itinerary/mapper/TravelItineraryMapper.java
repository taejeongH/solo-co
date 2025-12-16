package com.ssafy.travel.itinerary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import com.ssafy.travel.itinerary.dto.ItineraryPlaceResponseDto;

import java.util.List;

@Mapper
public interface TravelItineraryMapper {

	void deleteByProjectId(@Param("projectId") Long projectId);

    void insertItineraryPlace(
        @Param("projectId") Long projectId,
        @Param("day") int day,
        @Param("orderNo") int orderNo,
        @Param("placeId") Long placeId
    );

    List<ItineraryPlaceResponseDto> findPlacesByProjectId(@Param("projectId") Long projectId);
}
