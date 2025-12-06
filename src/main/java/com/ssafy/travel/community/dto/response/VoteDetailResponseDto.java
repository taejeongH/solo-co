package com.ssafy.travel.community.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class VoteDetailResponseDto {
    private Long voteId;
    private String question;
    private List<VoteOptionResponseDto> options;
    private Integer totalVotes;
    private Boolean hasVoted;
}
