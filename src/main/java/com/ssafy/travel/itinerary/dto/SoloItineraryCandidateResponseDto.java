package com.ssafy.travel.itinerary.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoloItineraryCandidateResponseDto extends ItineraryCandidateResponseDto {

    private String summary;
    private String recommendation;
    private SoloItineraryScoreDto soloScore;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class SoloItineraryScoreDto {
        private int totalScore;
        private int safety;
        private int transportAccessibility;
        private int routeSimplicity;
        private int landmarkAccessibility;
        private int soloDiningDifficulty;
        private Map<String, String> scoreJustification;
    }
}
