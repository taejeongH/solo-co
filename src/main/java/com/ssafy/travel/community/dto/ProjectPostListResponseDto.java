package com.ssafy.travel.community.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class ProjectPostListResponseDto {

    private Long postId;

    private String title;
    private String contentPreview;

    private Long authorId;
    private String authorName;
    private String authorProfileImage;

    private List<String> images;   // 대표 이미지 (여러개 가능)
    private List<String> tags;
}
