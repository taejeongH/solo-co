package com.ssafy.travel.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private String createdAt;
    private Boolean canDelete; // 삭제 가능 여부
}
