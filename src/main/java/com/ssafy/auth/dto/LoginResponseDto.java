package com.ssafy.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String username;
    private String email;
    private String name;
}
