package com.ssafy.travel.community.entity;

import lombok.Data;

@Data
public class ProjectPostComment {
	private Long commentId;
	private Long postId;
	private Long userId;
	private String content;
	private String createdAt;
	private String updatedAt;
}
