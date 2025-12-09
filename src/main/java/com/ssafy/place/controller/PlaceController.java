package com.ssafy.place.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.place.dto.PlaceDetailDto;
import com.ssafy.place.dto.PlaceDto;
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
    public ResponseEntity<PlaceSearchResponseDto> searchPlaces(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String nextPageToken
    ) {
        PlaceSearchResponseDto response = placeService.searchPlaces(query, location, type, nextPageToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{placeId}/brief")
    @Operation(summary = "장소 간략 정보 조회 (Google Places API)")
    public ResponseEntity<?> getPlaceBriefDetails(
            @PathVariable String placeId,
            @RequestParam Long projectId
    ) {
        return ResponseEntity.ok(placeService.getPlaceBriefDetails(placeId, projectId));
    }

    @GetMapping("/{placeId}/details")
    @Operation(summary = "장소 상세 정보 조회 (Google Places API)")
    public ResponseEntity<?> getPlaceFullDetails(
            @PathVariable String placeId,
            @RequestParam Long projectId
    ) {
        return ResponseEntity.ok(placeService.getPlaceFullDetails(placeId, projectId));
    }
}
