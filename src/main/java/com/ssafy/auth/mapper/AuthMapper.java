package com.ssafy.auth.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.auth.entity.User;

@Mapper
public interface AuthMapper {
    User findByUsername(String username);
    User findByRefreshToken(String refreshToken);
    int insertUser(User user);
    void updateRefreshToken(@Param("userId") Long userId, @Param("refreshToken") String refreshToken);
}