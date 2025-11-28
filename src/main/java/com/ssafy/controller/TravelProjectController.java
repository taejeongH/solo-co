package com.ssafy.controller;

import com.ssafy.dto.response.TravelProjectResponseDto;
import com.ssafy.service.TravelProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

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
    public ResponseEntity<List<TravelProjectResponseDto>> getMyProjects(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");

        List<TravelProjectResponseDto> projects = travelProjectService.getMyProjects(token);

        return ResponseEntity.ok(projects);
    }
}
