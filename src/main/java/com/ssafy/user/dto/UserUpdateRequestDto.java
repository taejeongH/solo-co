package com.ssafy.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    private String name;
    private String password;
    private String email;
}
