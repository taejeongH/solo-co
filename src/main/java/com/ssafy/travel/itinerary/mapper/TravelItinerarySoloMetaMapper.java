package com.ssafy.travel.itinerary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.ssafy.travel.itinerary.dto.SoloItineraryCandidateResponseDto;

@Mapper
public interface TravelItinerarySoloMetaMapper {
	
	void deleteByProjectId(@Param("projectId") Long projectId);

    void insertSoloMeta(
        @Param("projectId") Long projectId,
        @Param("solo") SoloItineraryCandidateResponseDto solo
    );
}
