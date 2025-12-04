package com.ssafy.travel.project.dto;

import lombok.Data;

@Data
public class InviteValidationResponseDto {
    private boolean valid;
    private Long projectId;
    private String projectTitle;
    private String location;
}
