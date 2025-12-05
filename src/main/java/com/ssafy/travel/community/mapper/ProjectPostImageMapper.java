package com.ssafy.travel.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectPostImageMapper {
	void insertImage(@Param("postId") Long postId,
            @Param("imageUrl") String imageUrl,
            @Param("sortOrder") int sortOrder);
}
