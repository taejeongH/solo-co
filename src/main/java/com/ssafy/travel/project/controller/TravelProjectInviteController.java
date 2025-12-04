package com.ssafy.travel.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.travel.project.dto.InviteJoinRequestDto;
import com.ssafy.travel.project.dto.InviteLinkResponseDto;
import com.ssafy.travel.project.dto.InviteValidationResponseDto;
import com.ssafy.travel.project.service.TravelProjectInviteService;
import com.ssafy.travel.project.service.TravelProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travels")
public class TravelProjectInviteController {
	
	private final TravelProjectService travelProjectService;
    private final TravelProjectInviteService inviteService;
	
    @PostMapping("/{projectId}/invite")
    @Operation(summary = "여행 프로젝트 초대 링크 생성", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<InviteLinkResponseDto> createInviteLink(
            @PathVariable Long projectId) {
    	Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    	
        InviteLinkResponseDto response = inviteService.createInviteLink(projectId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/invite/validate")
    @Operation(summary = "여행 프로젝트 초대 링크 검증", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> validateInvite(@RequestParam String code) {

        InviteValidationResponseDto result = inviteService.validateInvite(code);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/invite/join")
    @Operation(summary = "여행 프로젝트 초대 링크 참여", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> joinProjectByInvite(
            @RequestBody InviteJoinRequestDto req) {
    	Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    	
        inviteService.joinProject(req.getCode(), userId);
        return ResponseEntity.ok("프로젝트 참여 완료");
    }
}
