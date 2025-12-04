package com.ssafy.travel.project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TravelProjectMemberMapper {
	void insertMember(@Param("projectId") Long projectId,
            @Param("userId") Long userId,
            @Param("role") String role);
}
