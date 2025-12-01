package com.ssafy.auth.entity;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String name;
    private String profileImage;
}