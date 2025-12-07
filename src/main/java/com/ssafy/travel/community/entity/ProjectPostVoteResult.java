package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPostVoteResult {
    private Long resultId;
    private Long voteId;
    private Long userId;
    private Long optionId;
    private String votedAt;
}
