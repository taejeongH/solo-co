package com.ssafy.travel.itinerary.controller;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.ai.dto.AutoGenerateResponse;
import com.ssafy.travel.itinerary.dto.ItineraryApplyRequestDto;
import com.ssafy.travel.itinerary.dto.ItineraryResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryUpdateRequestDto;
import com.ssafy.travel.itinerary.service.TravelItineraryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class TravelItineraryController {

    private final TravelItineraryService itineraryService;
    private final ChatClient chatClient;

    @DeleteMapping("/{projectId}/itinerary")
    @Operation(summary = "여행 경로 삭제", description = "생성된 여행 경로를 삭제합니다.", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<String> deleteItinerary(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        itineraryService.deleteItinerary(projectId, userDetails.getUserId());
        return ResponseEntity.ok("여행 경로 삭제 완료");
    }

    @GetMapping("/{projectId}/itinerary")
    @Operation(summary = "여행 경로 조회", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<ItineraryResponseDto> getItinerary(@PathVariable long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ItineraryResponseDto itinerary = itineraryService.getItinerary(projectId, userDetails.getUserId());
        return ResponseEntity.ok(itinerary);
    }

    @PostMapping("/{projectId}/itinerary/auto-generate")
    @Operation(summary = "여행 경로 AI 추천", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> autoGenerate(@PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        AutoGenerateResponse response = itineraryService.autoGenerate(projectId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/itinerary/candidates/{aiResultId}")
    @Operation(summary = "여행 경로 AI 후보 조회", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> getAiCandidates(
            @PathVariable Long projectId,
            @PathVariable String aiResultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(itineraryService.getAiCandidates(projectId, userDetails.getUserId(), aiResultId));
    }

    @PostMapping("/{projectId}/itinerary/ai-select")
    @Operation(summary = "여행 경로 선택", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> selectAIItinerary(
            @PathVariable Long projectId,
            @RequestBody ItineraryApplyRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        itineraryService.applySelectedCandidate(userDetails.getUserId(), projectId, request);
        return ResponseEntity.ok("경로 저장 완료");
    }

    @PutMapping("/{projectId}/itinerary")
    @Operation(summary = "경로 수정", description = "경로 전체를 수정합니다. 요청 바디에 포함된 장소 목록으로 경로가 대체됩니다.", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<String> updateItinerary(
            @PathVariable Long projectId,
            @RequestBody ItineraryUpdateRequestDto updateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        itineraryService.updateItinerary(projectId, userDetails.getUserId(), updateRequest);
        return ResponseEntity.ok("경로 수정 완료");
    }

    @GetMapping("/test-ai")
    public String testAi() {
        return chatClient.prompt()
                .user("hello world")
                .call()
                .content();
    }
}
