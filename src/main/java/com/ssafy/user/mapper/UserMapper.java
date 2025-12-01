package com.ssafy.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.auth.entity.User;

@Mapper
public interface UserMapper {

    User findById(Long id);

    int updateUser(
        Long userId,
        String name,
        String profileImage,
        String password,
        String email
    );
    
    void deleteUser(Long userId);
}
