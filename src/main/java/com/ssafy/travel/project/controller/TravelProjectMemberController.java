package com.ssafy.travel.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.travel.project.service.TravelProjectMemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travels/{projectId}/members")
@RequiredArgsConstructor
public class TravelProjectMemberController {

    private final TravelProjectMemberService memberService;

    @GetMapping
    @Operation(summary = "여행 프로젝트 멤버 조회", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> getMembers(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId) {

        return ResponseEntity.ok(memberService.getMembers(projectId, user.getUserId()));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "여행 프로젝트 멤버 삭제", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails user) {

        memberService.removeMember(projectId, memberId, user.getUserId());

        return ResponseEntity.ok("해당 멤버를 강퇴했습니다.");
    }
}
