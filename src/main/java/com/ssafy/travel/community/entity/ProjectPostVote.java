package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPostVote {
    private Long voteId;
    private Long postId;
    private String question;
    private Boolean multipleChoice;
}
