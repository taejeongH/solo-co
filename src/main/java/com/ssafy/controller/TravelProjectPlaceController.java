package com.ssafy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.entity.TravelProjectPlace;
import com.ssafy.service.TravelProjectPlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/travels/{projectId}/places")
@RequiredArgsConstructor
public class TravelProjectPlaceController {

    private final TravelProjectPlaceService placeService;

    @PostMapping
    @Operation(summary = "여행 장소 추가", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> addPlace(
            @PathVariable Long projectId,
            @RequestBody TravelProjectPlace place) {

        placeService.addPlace(projectId, place);
        return ResponseEntity.ok("장소 추가 완료");
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "여행 장소 삭제", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> deletePlace(
            @PathVariable Long projectId,
            @PathVariable Long placeId) {

        placeService.deletePlace(projectId, placeId);
        return ResponseEntity.ok("장소 삭제 완료");
    }

    @GetMapping
    @Operation(summary = "여행 장소 조회", security = @SecurityRequirement(name = "JWT Auth"))
    public ResponseEntity<?> getPlaces(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(placeService.getPlaces(projectId));
    }
}
