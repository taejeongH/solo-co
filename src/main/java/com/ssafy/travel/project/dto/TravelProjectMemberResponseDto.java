package com.ssafy.travel.project.dto;

import lombok.Data;

@Data
public class TravelProjectMemberResponseDto {

    private Long userId;
    private String username;
    private String name;
    private String profileImage;

    private String role; // OWNER / MEMBER
}
