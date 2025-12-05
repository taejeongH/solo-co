package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPostVoteOption {
    private Long optionId;
    private Long voteId;
    private String optionText;
    private int orderNo;
}
