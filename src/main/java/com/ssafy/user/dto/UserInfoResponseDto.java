package com.ssafy.user.dto;

import lombok.Data;

@Data
public class UserInfoResponseDto {
    private String id; // username
    private String name; // 사용자 실제 이름
    private String email;
    private String profileImage;
}
