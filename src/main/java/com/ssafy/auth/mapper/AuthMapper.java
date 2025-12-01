package com.ssafy.auth.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.ssafy.auth.entity.User;

@Mapper
public interface AuthMapper {
    User findByUsername(String username);
    int insertUser(User user);
}