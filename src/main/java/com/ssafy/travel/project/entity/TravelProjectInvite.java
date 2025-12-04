package com.ssafy.travel.project.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TravelProjectInvite {
    private Long inviteId;
    private Long projectId;
    private String inviteCode;
    private LocalDateTime expiresAt;
    private Integer maxUses;
    private Integer useCount;
    private LocalDateTime createdAt;
}
