package com.ssafy.dto.response;

import lombok.Data;

@Data
public class UserInfoResponseDto {
    private String id;           // username
    private String name;         // 사용자 실제 이름
    private String email;
    private String profileImage;
}
