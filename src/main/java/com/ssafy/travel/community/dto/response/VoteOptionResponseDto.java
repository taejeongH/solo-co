package com.ssafy.travel.community.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VoteOptionResponseDto {
    private Long optionId;
    private String text;
    private Integer count;
}
