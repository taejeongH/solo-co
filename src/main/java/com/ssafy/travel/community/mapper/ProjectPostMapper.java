package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.travel.community.dto.ProjectPostListResponseDto;
import com.ssafy.travel.community.entity.ProjectPost;

@Mapper
public interface ProjectPostMapper {
	void insertPost(ProjectPost post);
	List<ProjectPostListResponseDto> findAllPostsByProjectId(Long projectId);
}
