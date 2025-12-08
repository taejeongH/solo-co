package com.ssafy.travel.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.entity.ProjectPostVoteResult;

@Mapper
public interface ProjectPostVoteResultMapper {
	int deleteResultsByVoteId(@Param("voteId") Long voteId);
	ProjectPostVoteResult findVoteByUser(@Param("voteId") Long voteId, @Param("userId") Long userId);
    void insertVoteResult(@Param("voteId") Long voteId, @Param("userId") Long userId, @Param("optionId") Long optionId);
    int countTotalVotes(@Param("voteId") Long voteId);
    int countVotesByOption(@Param("optionId") Long optionId);
    int deleteVoteByUser(@Param("voteId") Long voteId, @Param("userId") Long userId);
}
