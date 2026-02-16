package com.ssafy.travel.community.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class VoteDetailResponseDto {
    private Long voteId;
    private String question;
    private List<VoteOptionResponseDto> options;
    private Integer totalVotes;
    private Boolean hasVoted;
}
