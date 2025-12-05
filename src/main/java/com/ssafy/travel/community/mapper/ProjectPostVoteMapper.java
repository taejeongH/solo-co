package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.travel.community.entity.ProjectPostVote;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;

@Mapper
public interface ProjectPostVoteMapper {
    void insertVote(ProjectPostVote vote);
    void insertOption(ProjectPostVoteOption option);
    Long findVoteIdByPostId(Long postId);
    List<ProjectPostVoteOption> findOptionsByVoteId(Long voteId);
}
