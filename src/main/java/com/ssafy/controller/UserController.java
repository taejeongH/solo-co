package com.ssafy.controller;

import com.ssafy.dto.request.UserUpdateRequestDto;
import com.ssafy.dto.response.UserResponseDto;
import com.ssafy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final AuthService userService;

    @Operation(
            summary = "회원 정보 수정",
            description = "로그인한 사용자가 닉네임/비밀번호/프로필을 수정합니다.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateUser(
            @RequestBody UserUpdateRequestDto dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // Bearer token 추출
        String token = authorizationHeader.replace("Bearer ", "");

        UserResponseDto response = userService.updateUser(token, dto);
        return ResponseEntity.ok(response);
    }
}
