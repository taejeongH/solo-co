package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.response.ProjectPostDetailResponseDto;
import com.ssafy.travel.community.dto.response.ProjectPostListResponseDto;
import com.ssafy.travel.community.entity.ProjectPost;

@Mapper
public interface ProjectPostMapper {
	void insertPost(ProjectPost post);
	List<ProjectPostListResponseDto> findAllPostsByProjectId(
			@Param("projectId") Long projectId,
			@Param("query") String query
	);
	ProjectPostDetailResponseDto findPostDetail(@Param("postId") Long postId);
	Long findPostAuthorId(@Param("postId") Long postId);
	int deletePost(@Param("postId") Long postId);
	int updatePostBasic(
			@Param("postId") Long postId, 
			@Param("title") String title, 
			@Param("content") String content);
}
