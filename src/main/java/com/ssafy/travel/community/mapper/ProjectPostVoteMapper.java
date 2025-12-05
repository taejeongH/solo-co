package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.VoteDetailDto;
import com.ssafy.travel.community.entity.ProjectPostVote;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;

@Mapper
public interface ProjectPostVoteMapper {
    void insertVote(ProjectPostVote vote);
    void insertOption(ProjectPostVoteOption option);
    Long findVoteIdByPostId(Long postId);
    VoteDetailDto findVoteByPostId(@Param("postId") Long postId);
    List<ProjectPostVoteOption> findOptionsByVoteId(Long voteId);
    Boolean hasUserVoted(
            @Param("voteId") Long voteId,
            @Param("userId") Long userId
        );
}
