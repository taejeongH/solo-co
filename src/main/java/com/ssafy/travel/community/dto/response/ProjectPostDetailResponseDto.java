package com.ssafy.travel.community.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectPostDetailResponseDto {
    private Long postId;
    private String title;
    private String content;

    private Long userId;
    private String userName;
    private String userProfileImage;

    private String createdAt;

    private List<String> images;
    private List<String> tags;

    private VoteDetailResponseDto vote;
    private List<CommentResponseDto> comments;
}