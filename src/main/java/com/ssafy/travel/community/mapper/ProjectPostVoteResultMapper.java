package com.ssafy.travel.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectPostVoteResultMapper {
	int deleteResultsByVoteId(@Param("voteId") Long voteId);
}
