package com.ssafy.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    private String name;
    private String password;
    private String profileImage;
    private String email;
}
