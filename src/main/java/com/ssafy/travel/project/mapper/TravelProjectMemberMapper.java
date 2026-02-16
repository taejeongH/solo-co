package com.ssafy.travel.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.project.dto.TravelProjectMemberResponseDto;
import com.ssafy.travel.project.entity.TravelProjectMember;

@Mapper
public interface TravelProjectMemberMapper {
	List<TravelProjectMemberResponseDto> findMembers(Long projectId);

	TravelProjectMember findOne(@Param("projectId") Long projectId, @Param("userId") Long userId);

	void deleteMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

	void insertMember(@Param("projectId") Long projectId, @Param("userId") Long userId, @Param("role") String role);

	boolean isMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

	int countMembersByProjectId(@Param("projectId") Long projectId);

	void deleteAllByProjectId(Long projectId);
}
