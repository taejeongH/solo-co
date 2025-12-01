package com.ssafy.travel.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.travel.project.dto.TravelProjectCreateRequestDto;
import com.ssafy.travel.project.dto.TravelProjectResponseDto;
import com.ssafy.travel.project.service.TravelProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travels")
public class TravelProjectController {

    private final TravelProjectService travelProjectService;

    @Operation(
            summary = "내 여행 목록 조회",
            description = "로그인한 사용자의 개인/그룹 여행 프로젝트 목록 조회",
            security = @SecurityRequirement(name = "JWT Auth")
    )
    @GetMapping
    public ResponseEntity<List<TravelProjectResponseDto>> getMyProjects() {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        List<TravelProjectResponseDto> projects = travelProjectService.getMyProjects(userId);

        return ResponseEntity.ok(projects);
    }
    
    @PostMapping
    @Operation(summary = "여행 프로젝트 생성", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<TravelProjectResponseDto> createProject(@RequestBody TravelProjectCreateRequestDto requestDto) {
    	Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        TravelProjectResponseDto result =
                travelProjectService.createProject(userId, requestDto);

        return ResponseEntity.status(201).body(result);
    }

}
