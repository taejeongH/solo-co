package com.ssafy.auth.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String username;
    private String password;
    private String email;
    private String name;
}

