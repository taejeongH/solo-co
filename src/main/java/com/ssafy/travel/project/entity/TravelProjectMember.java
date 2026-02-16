package com.ssafy.travel.project.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TravelProjectMember {
    private Long memberId; // PK
    private Long projectId; // 프로젝트 ID
    private Long userId; // 유저 ID
    private String role; // OWNER / MEMBER
    private LocalDateTime createdAt;
}
