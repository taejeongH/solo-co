package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.VoteOptionDto;

@Mapper
public interface PostVoteOptionMapper {

    List<VoteOptionDto> findVoteOptions(@Param("voteId") Long voteId);
}
