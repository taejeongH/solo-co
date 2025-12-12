package com.ssafy.travel.itinerary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.ssafy.travel.itinerary.dto.GroupItineraryCandidateResponseDto;

@Mapper
public interface TravelItineraryGroupMetaMapper {
	void deleteByProjectId(@Param("projectId") Long projectId);

    void insertGroupMeta(
        @Param("projectId") Long projectId,
        @Param("group") GroupItineraryCandidateResponseDto group
    );
}
