package com.ssafy.user.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.user.dto.UserInfoResponseDto;
import com.ssafy.user.dto.UserResponseDto;
import com.ssafy.user.dto.UserUpdateRequestDto;
import com.ssafy.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "회원 정보 수정",
            description = "로그인한 사용자가 닉네임/비밀번호/프로필을 수정합니다.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @PutMapping
    public ResponseEntity<UserResponseDto> updateUser(@AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request,
            @RequestPart("dto") UserUpdateRequestDto dto, @RequestPart("file") MultipartFile file) throws IOException {
    	Long userId = user.getUserId();

        UserResponseDto response = userService.updateUser(userId, dto, file);
        return ResponseEntity.ok(response);
    }
    
    
    @Operation(
            summary = "회원 정보 삭제",
            description = "로그인한 사용자 회원 정보 삭제.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request) {
    	Long userId = user.getUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
    
    
    @Operation(
            summary = "회원 정보 조회",
            description = "로그인한 사용자 회원 정보 조회.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @GetMapping
    public ResponseEntity<UserInfoResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request) {
    	Long userId = user.getUserId();
        UserInfoResponseDto response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }



}
