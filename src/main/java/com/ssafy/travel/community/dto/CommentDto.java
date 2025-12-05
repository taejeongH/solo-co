package com.ssafy.travel.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Builder
public class CommentDto {
    private Long commentId;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private String createdAt;
}
