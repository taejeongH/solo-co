package com.ssafy.controller;

import com.ssafy.dto.request.UserUpdateRequestDto;
import com.ssafy.dto.response.UserResponseDto;
import com.ssafy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody UserUpdateRequestDto dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // Bearer token 추출
        String token = authorizationHeader.replace("Bearer ", "");

        UserResponseDto response = userService.updateUser(token, dto);
        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "회원 정보 삭제",
            description = "로그인한 사용자 회원 정보 삭제.",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @DeleteMapping
    public ResponseEntity<String> deleteUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        userService.deleteUser(token);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

}
