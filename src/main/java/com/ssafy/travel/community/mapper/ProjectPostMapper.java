package com.ssafy.travel.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.ssafy.travel.community.entity.ProjectPost;

@Mapper
public interface ProjectPostMapper {
	void insertPost(ProjectPost post);
}
