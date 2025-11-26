package com.ssafy.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.ssafy.entity.User;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
}