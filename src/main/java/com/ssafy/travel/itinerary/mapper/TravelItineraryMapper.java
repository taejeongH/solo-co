package com.ssafy.travel.itinerary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TravelItineraryMapper {

	void deleteByProjectId(@Param("projectId") Long projectId);

    void insertItineraryPlace(
        @Param("projectId") Long projectId,
        @Param("day") int day,
        @Param("orderNo") int orderNo,
        @Param("placeId") Long placeId
    );
}
