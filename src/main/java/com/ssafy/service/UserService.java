package com.ssafy.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.authentication.JwtTokenProvider;
import com.ssafy.dto.request.UserUpdateRequestDto;
import com.ssafy.dto.response.UserResponseDto;
import com.ssafy.entity.User;
import com.ssafy.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto updateUser(String token, UserUpdateRequestDto dto) {

        // 1. 토큰에서 userId 추출
    	System.out.println(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // 2. 기존 데이터 조회
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 3. 변경될 필드만 세팅
        String nickname = dto.getName() != null ? dto.getName() : user.getName();
        String email = dto.getEmail() != null ? dto.getEmail() : user.getEmail();
        String profile = dto.getProfileImage() != null ? dto.getProfileImage() : user.getProfileImage();

        // 비밀번호는 선택 변경
        String password = user.getPassword();
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            password = passwordEncoder.encode(dto.getPassword());
        }

        // 4. DB Update
        userMapper.updateUser(userId, nickname, profile, password);

        // 5. 응답 DTO 구성
        User updated = userMapper.findById(userId);

        UserResponseDto response = new UserResponseDto();
        response.setId(updated.getUserId());
        response.setEmail(updated.getEmail());
        response.setNickname(updated.getName());
        response.setProfile(updated.getProfileImage());

        return response;
    }
}
