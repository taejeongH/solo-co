package com.ssafy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dto.request.ItineraryApplyRequestDto;
import com.ssafy.dto.response.AutoGenerateResponse;
import com.ssafy.service.TravelItineraryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class TravelItineraryController {

    private final TravelItineraryService itineraryService;

    @PostMapping("/{projectId}/itinerary/auto-generate")
    @Operation(summary = "여행 경로 AI 추천", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> autoGenerate(@PathVariable Long projectId) {

        AutoGenerateResponse response = itineraryService.autoGenerate(projectId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{projectId}/itinerary/ai-select")
    @Operation(summary = "여행 경로 선택", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> selectAIItinerary(
            @PathVariable Long projectId,
            @RequestBody ItineraryApplyRequestDto request) {

        itineraryService.applySelectedCandidate(projectId, request);
        return ResponseEntity.ok("AI itinerary applied");
    }

}
