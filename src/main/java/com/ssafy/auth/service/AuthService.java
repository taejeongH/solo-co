package com.ssafy.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.auth.dto.LoginRequestDto;
import com.ssafy.auth.dto.LoginResponseDto;
import com.ssafy.auth.dto.SignupRequestDto;
import com.ssafy.auth.entity.User;
import com.ssafy.auth.mapper.AuthMapper;
import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.global.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userMapper.findByUsername(requestDto.getUsername());

        if (user == null || !passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        String token = jwtTokenProvider.createToken(user.getUserId(), user.getUsername(), user.getEmail());

        return LoginResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
    
    public void signup(SignupRequestDto dto) {
        // username 중복 체크
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());

        userMapper.insertUser(user);
    }
}

