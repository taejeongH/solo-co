package com.ssafy.travel.place.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.travel.place.dto.ProjectPlaceListResponseDto;
import com.ssafy.travel.place.service.TravelProjectPlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travels/{projectId}/places")
@RequiredArgsConstructor
public class TravelProjectPlaceController {

    private final TravelProjectPlaceService placeService;

    @PostMapping("/{googlePlaceId}")
    @Operation(summary = "여행 장소 추가", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> addPlace(
            @PathVariable Long projectId,
            @PathVariable String googlePlaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        placeService.addPlace(projectId, googlePlaceId, userDetails.getUserId(), "CONFIRMED");
        return ResponseEntity.ok("장소 추가 완료");
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "여행 장소 삭제", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> deletePlace(
            @PathVariable Long projectId,
            @PathVariable Long placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        placeService.deletePlace(projectId, placeId, userDetails.getUserId());
        return ResponseEntity.ok("장소 삭제 완료");
    }

    @GetMapping
    @Operation(summary = "여행 장소 목록 조회", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<List<ProjectPlaceListResponseDto>> getPlaces(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        List<ProjectPlaceListResponseDto> places = placeService.getPlaces(projectId, userDetails.getUserId(), sortBy,
                order);
        return ResponseEntity.ok(places);
    }
}
