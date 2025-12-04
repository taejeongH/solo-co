package com.ssafy.travel.project.dto;

import lombok.Data;

@Data
public class InviteLinkResponseDto {
    private Long projectId;
    private String inviteCode;
    private String inviteUrl;
}
