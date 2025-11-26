package com.ssafy.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.ssafy.entity.User;

@Mapper
public interface AuthMapper {
    User findByUsername(String username);
    int insertUser(User user);
}