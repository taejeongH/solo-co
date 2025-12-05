package com.ssafy.travel.community.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class VoteDetailDto {
    private Long voteId;
    private String question;
    private List<VoteOptionDto> options;
    private Integer totalVotes;
    private Boolean hasVoted;
}
