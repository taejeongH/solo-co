package com.ssafy.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private Long userId;
    private String email;
    private String name;
    private String profileImage;
}
