package com.ssafy.travel.project.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.travel.project.dto.InviteJoinRequestDto;
import com.ssafy.travel.project.dto.InviteLinkResponseDto;
import com.ssafy.travel.project.dto.InviteValidationResponseDto;
import com.ssafy.travel.project.dto.TravelProjectRequestDto;
import com.ssafy.travel.project.dto.TravelProjectResponseDto;
import com.ssafy.travel.project.service.TravelProjectInviteService;
import com.ssafy.travel.project.service.TravelProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travels")
public class TravelProjectController {

    private final TravelProjectService travelProjectService;
    private final TravelProjectInviteService inviteService;
    
    @PutMapping("/{projectId}")
    @Operation(summary = "여행 프로젝트 수정", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> getPlaces(
    		@AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long projectId, 
            @RequestPart TravelProjectRequestDto dto, 
            @RequestPart(required = false) MultipartFile thumbnail) throws IOException {
    	Long userId = user.getUserId();
    	
    	TravelProjectResponseDto result = travelProjectService.updateProject(projectId, userId, dto, thumbnail);
        return ResponseEntity.ok(result);
    }
    
    
    @Operation(
            summary = "내 여행 목록 조회",
            description = "로그인한 사용자의 개인/그룹 여행 프로젝트 목록 조회",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @GetMapping
    public ResponseEntity<List<TravelProjectResponseDto>> getMyProjects(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();

        List<TravelProjectResponseDto> projects = travelProjectService.getMyProjects(userId);

        return ResponseEntity.ok(projects);
    }
    
    @PostMapping
    @Operation(summary = "여행 프로젝트 생성", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<TravelProjectResponseDto> createProject(
    		@AuthenticationPrincipal CustomUserDetails user,
    		@RequestPart TravelProjectRequestDto dto, 
    		@RequestPart MultipartFile thumbnail) throws IOException {
    	Long userId = user.getUserId();

        TravelProjectResponseDto result = travelProjectService.createProject(userId, dto, thumbnail);

        return ResponseEntity.status(201).body(result);
    }

}
