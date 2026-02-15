package com.ssafy.user.service;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.auth.entity.User;
import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.global.service.S3Service;
import com.ssafy.user.dto.UserInfoResponseDto;
import com.ssafy.user.dto.UserResponseDto;
import com.ssafy.user.dto.UserUpdateRequestDto;
import com.ssafy.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3service;

    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto dto, MultipartFile file) throws IOException {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String nickname = dto.getName();
        String email = dto.getEmail();
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = s3service.upload(file, "profile");
        }
        String password = null;
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            password = passwordEncoder.encode(dto.getPassword());
        }

        userMapper.updateUser(userId, nickname, imageUrl, password, email);

        User updated = userMapper.findById(userId);

        UserResponseDto response = new UserResponseDto();
        response.setUserId(updated.getUserId());
        response.setEmail(updated.getEmail());
        response.setName(updated.getName());
        response.setProfileImage(s3service.generatePresignedUrl(updated.getProfileImage()));

        return response;
    }

    public void deleteUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        userMapper.deleteUser(userId);
    }

    public UserInfoResponseDto getMyInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoResponseDto dto = new UserInfoResponseDto();
        dto.setId(user.getUsername()); // 예시에서 id = username
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setProfileImage(s3service.generatePresignedUrl(user.getProfileImage()));

        return dto;
    }

}
