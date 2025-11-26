package com.ssafy.dto.request;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String username;
    private String password;
    private String email;
    private String name;
    private String profileImage; // 선택 가능
}

