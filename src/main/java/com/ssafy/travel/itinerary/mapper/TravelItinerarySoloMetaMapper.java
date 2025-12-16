package com.ssafy.travel.itinerary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.ssafy.travel.itinerary.dto.SoloItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.SoloItineraryMetaResponseDto;

@Mapper
public interface TravelItinerarySoloMetaMapper {
	
	void deleteByProjectId(@Param("projectId") Long projectId);

    void insertSoloMeta(
        @Param("projectId") Long projectId,
        @Param("solo") SoloItineraryCandidateResponseDto solo
    );

    SoloItineraryMetaResponseDto findMetaByProjectId(@Param("projectId") Long projectId);
}
