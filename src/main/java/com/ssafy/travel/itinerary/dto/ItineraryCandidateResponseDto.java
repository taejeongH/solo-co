package com.ssafy.travel.itinerary.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItineraryCandidateResponseDto {

    private int routeType;
    private List<ItineraryDayDto> days;
    private String reason;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ItineraryDayDto {
        private int day;
        private List<ItineraryPlaceDto> places;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ItineraryPlaceDto {
        private Long placeId;
        private String placeName;
        private String placeAddress;
        private String placeType;
        private String thumbnail;
        private Double latitude;
        private Double longitude;
        private boolean newPlace;
    }
}
