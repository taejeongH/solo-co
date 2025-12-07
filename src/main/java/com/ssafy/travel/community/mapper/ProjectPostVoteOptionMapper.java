package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.response.VoteOptionResponseDto;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;

@Mapper
public interface ProjectPostVoteOptionMapper {
    List<VoteOptionResponseDto> findVoteOptions(@Param("voteId") Long voteId);
    ProjectPostVoteOption findOptionById(@Param("optionId") Long optionId);
    int deleteOptionsByVoteId(@Param("voteId") Long voteId);
}
