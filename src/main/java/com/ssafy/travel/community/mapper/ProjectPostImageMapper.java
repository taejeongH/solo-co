package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectPostImageMapper {
	void insertImage(@Param("postId") Long postId,
            @Param("imageUrl") String imageUrl,
            @Param("sortOrder") int sortOrder);
	
	List<String> findImagesByPostId(@Param("postId") Long postId);
}
