package com.ssafy.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.authentication.JwtTokenProvider;
import com.ssafy.dto.request.LoginRequestDto;
import com.ssafy.dto.request.SignupRequestDto;
import com.ssafy.dto.response.LoginResponseDto;
import com.ssafy.entity.User;
import com.ssafy.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userMapper.findByUsername(requestDto.getUsername());

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getUsername());

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
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setProfileImage(dto.getProfileImage());

        userMapper.insertUser(user);
    }
}

