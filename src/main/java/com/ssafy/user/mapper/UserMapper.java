package com.ssafy.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.auth.entity.User;

@Mapper
public interface UserMapper {

    User findById(Long id);

    int updateUser(
        @Param("userId") Long userId,
        @Param("name") String name,
        @Param("profileImage") String profileImage,
        @Param("password") String password,
        @Param("email") String email
    );
    
    void deleteUser(Long userId);
}
