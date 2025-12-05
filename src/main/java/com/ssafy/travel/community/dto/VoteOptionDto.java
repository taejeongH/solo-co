package com.ssafy.travel.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Builder
public class VoteOptionDto {
    private Long optionId;
    private String text;
    private Integer count;
}
