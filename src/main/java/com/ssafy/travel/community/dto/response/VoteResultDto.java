package com.ssafy.travel.community.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VoteResultDto {
    private int totalVotes;
    private List<VoteOptionResultDto> options;
}