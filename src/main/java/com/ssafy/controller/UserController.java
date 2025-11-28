package com.ssafy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dto.request.UserUpdateRequestDto;
import com.ssafy.dto.response.UserInfoResponseDto;
import com.ssafy.dto.response.UserResponseDto;
import com.ssafy.service.UserService;

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
    public ResponseEntity<UserResponseDto> updateUser(
            HttpServletRequest request,
            @RequestBody UserUpdateRequestDto dto
    ) {
    	Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UserResponseDto response = userService.updateUser(userId, dto);
        return ResponseEntity.ok(response);
    }
    
    
    @Operation(
            summary = "회원 정보 삭제",
            description = "로그인한 사용자 회원 정보 삭제.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @DeleteMapping
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
    	Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        userService.deleteUser(userId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
    
    
    @Operation(
            summary = "회원 정보 조회",
            description = "로그인한 사용자 회원 정보 조회.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @GetMapping
    public ResponseEntity<UserInfoResponseDto> getMyInfo(HttpServletRequest request) {
    	Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        UserInfoResponseDto response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }



}
