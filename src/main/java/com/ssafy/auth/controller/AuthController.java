package com.ssafy.auth.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.ssafy.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpiration;

    @Operation(summary = "로그인", description = "사용자 인증 및 Access Token, Refresh Token 발급")
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
    
    @Operation(summary = "회원가입", description = "새로운 사용자 계정 생성")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto dto) {
    	authService.signup(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 사용하여 새로운 Access Token 발급")
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenDto tokenDto = authService.reissueToken(refreshToken);
        return ResponseEntity.ok(tokenDto);
    }

    @Operation(summary = "로그아웃", description = "사용자의 Refresh Token 무효화 및 쿠키 삭제")
    @PostMapping("/logout")
    @SecurityRequirement(name = "JWT Auth")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 1. DB에서 Refresh Token 제거
        authService.logout(userDetails.getUserId());

        // 2. 클라이언트의 쿠키를 삭제하기 위해 Max-Age=0인 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
            .maxAge(0)
            .path("/")
            .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("로그아웃 되었습니다.");
    }
}
