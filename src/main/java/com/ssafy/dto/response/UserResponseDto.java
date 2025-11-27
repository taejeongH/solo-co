package com.ssafy.dto.response;

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
