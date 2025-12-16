package com.ssafy.travel.itinerary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SoloItineraryMetaResponseDto implements ItineraryMetaResponseDto {
    private double totalScore;
    private double safety;
    private double transportAccessibility;
    private double routeSimplicity;
    private double landmarkAccessibility;
    private double soloDiningDifficulty;
    private String summary;
}

