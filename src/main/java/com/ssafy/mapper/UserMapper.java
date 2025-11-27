package com.ssafy.mapper;

import com.ssafy.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findById(Long id);

    int updateUser(
            @Param("id") Long id,
            @Param("nickname") String nickname,
            @Param("profile") String profile,
            @Param("password") String password
    );
}
