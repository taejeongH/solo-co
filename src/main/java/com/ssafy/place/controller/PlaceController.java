package com.ssafy.place.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.place.dto.PlaceSearchResponseDto;
import com.ssafy.place.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "JWT Auth")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/search")
    @Operation(summary = "장소 검색 (Google Places API)")
    public CompletableFuture<ResponseEntity<PlaceSearchResponseDto>> searchPlaces(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String nextPageToken) {
        return placeService.searchPlaces(query, location, type, nextPageToken)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{placeId}/brief")
    @Operation(summary = "장소 간략 정보 조회 (Google Places API)")
    public CompletableFuture<ResponseEntity<?>> getPlaceBriefDetails(
            @PathVariable String placeId,
            @RequestParam Long projectId) {
        return placeService.getPlaceBriefDetails(placeId, projectId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{placeId}/details")
    @Operation(summary = "장소 상세 정보 조회 (Google Places API)")
    public CompletableFuture<ResponseEntity<?>> getPlaceFullDetails(
            @PathVariable String placeId,
            @RequestParam Long projectId) {
        return placeService.getPlaceFullDetails(placeId, projectId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/recommendations/solo-dining")
    @Operation(summary = "혼밥하기 좋은 음식점 추천 (Google Places API)")
    public CompletableFuture<ResponseEntity<?>> recommendSoloDining(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") int radius) {
        return placeService.recommendSoloDining(latitude, longitude, radius)
                .thenApply(ResponseEntity::ok);
    }
}
