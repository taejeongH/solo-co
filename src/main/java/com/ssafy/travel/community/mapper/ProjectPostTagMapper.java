package com.ssafy.travel.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectPostTagMapper {
	void insertTag(@Param("postId") Long postId,
            @Param("tag") String tag);
}
