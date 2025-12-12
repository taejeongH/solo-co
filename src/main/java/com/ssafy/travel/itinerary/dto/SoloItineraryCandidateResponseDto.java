package com.ssafy.travel.itinerary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoloItineraryCandidateResponseDto extends ItineraryCandidateResponseDto{
	
	private SoloItineraryScoreDto soloScore;
    private String summary;
    private String recommendation;
    private ItineraryRouteMetaDto routeMeta;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class SoloItineraryScoreDto {
        private int totalScore;
        private int safety;
        private int transportAccessibility;
        private int routeSimplicity;
        private int landmarkAccessibility;
        private int soloDiningDifficulty;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ItineraryRouteMetaDto {
        private String reason;
    }
}
