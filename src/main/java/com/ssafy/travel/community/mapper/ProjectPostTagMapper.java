package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectPostTagMapper {
	void insertTag(@Param("postId") Long postId,
			@Param("tag") String tag);

	List<String> findTagsByPostId(Long postId);

	int deleteTagsByPostId(@Param("postId") Long postId);
}
