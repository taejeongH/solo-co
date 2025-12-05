package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPostImage {
    private Long imageId;
    private Long postId;
    private String imageUrl;
    private Integer orderNo;
}
