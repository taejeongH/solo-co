package com.ssafy.travel.community.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VoteOptionResultDto {
    private Long optionId;
    private String content;
    private int voteCount;
}