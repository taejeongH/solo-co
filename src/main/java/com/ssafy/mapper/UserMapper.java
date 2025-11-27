package com.ssafy.mapper;

import com.ssafy.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
