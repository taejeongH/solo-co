package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPostTag {
    private Long tagId;
    private Long postId;
    private String tag;
}
