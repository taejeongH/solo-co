package com.ssafy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TravelItineraryMapper {

	void deleteByProjectId(@Param("projectId") Long projectId);

	void insertItinerary(
		    @Param("projectId") Long projectId,
		    @Param("day") int day,
		    @Param("places") List<String> places
		);
}
