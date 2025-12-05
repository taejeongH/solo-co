package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPost {
    private Long postId;
    private Long projectId;
    private Long authorId;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
}
