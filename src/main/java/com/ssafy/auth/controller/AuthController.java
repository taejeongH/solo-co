package com.ssafy.auth.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.auth.dto.LoginRequestDto;
import com.ssafy.auth.dto.LoginResponseDto;
import com.ssafy.auth.dto.SignupRequestDto;
import com.ssafy.auth.dto.TokenDto;
import com.ssafy.auth.entity.User;
import com.ssafy.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpiration;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        Map<String, Object> result = authService.login(requestDto);
        
        TokenDto tokenDto = (TokenDto) result.get("tokens");
        User user = (User) result.get("user");

        // 🔥 Refresh Token을 Secure, HttpOnly 쿠키로 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
            .maxAge(refreshExpiration / 1000) // 초 단위로 변경
            .path("/")
            .secure(true)
            .httpOnly(true)
            .build();

        LoginResponseDto responseDto = LoginResponseDto.builder()
            .accessToken(tokenDto.getAccessToken())
            .username(user.getUsername())
            .email(user.getEmail())
            .name(user.getName())
            .build();
            
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseDto);
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto dto) {
    	authService.signup(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenDto tokenDto = authService.reissueToken(refreshToken);
        return ResponseEntity.ok(tokenDto);
    }
}

