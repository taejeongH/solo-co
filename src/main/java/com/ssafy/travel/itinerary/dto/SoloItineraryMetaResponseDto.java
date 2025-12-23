package com.ssafy.travel.itinerary.dto;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SoloItineraryMetaResponseDto implements ItineraryMetaResponseDto {
    private double totalScore;
    private double safety;
    private double transportAccessibility;
    private double routeSimplicity;
    private double landmarkAccessibility;
    private double soloDiningDifficulty;
    private String summary;
    private String recommendation; // Added recommendation field
    private Map<String, String> scoreJustification;
}

