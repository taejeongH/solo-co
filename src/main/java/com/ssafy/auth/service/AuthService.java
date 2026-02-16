package com.ssafy.auth.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.auth.dto.LoginRequestDto;
import com.ssafy.auth.dto.SignupRequestDto;
import com.ssafy.auth.dto.TokenDto;
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

    @Transactional
    public Map<String, Object> login(LoginRequestDto requestDto) {
        User user = userMapper.findByUsername(requestDto.getUsername());

        if (user == null || !passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        // 1. Access Token, Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

        // 2. Refresh Token DB 저장
        userMapper.updateRefreshToken(user.getUserId(), refreshToken);

        // 3. 토큰과 사용자 정보를 Map에 담아 반환
        TokenDto tokenDto = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("tokens", tokenDto);
        result.put("user", user);

        return result;
    }

    @Transactional
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

    @Transactional
    public void logout(Long userId) {
        userMapper.updateRefreshToken(userId, null);
    }

    @Transactional(readOnly = true)
    public TokenDto reissueToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증 (만료, 손상 등)
        jwtTokenProvider.validateToken(refreshToken);

        // 2. DB에서 Refresh Token으로 사용자 조회
        User user = userMapper.findByRefreshToken(refreshToken);
        if (user == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "DB에 존재하지 않는 Refresh Token 입니다.");
        }

        // 3. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername(),
                user.getEmail());

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .build();
    }
}
