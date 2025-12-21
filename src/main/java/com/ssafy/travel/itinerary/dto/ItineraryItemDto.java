package com.ssafy.travel.itinerary.dto;

import lombok.Data;

@Data
public class ItineraryItemDto {
    // For existing places
    private Long placeId;
    // For new places to be added
    private String googlePlaceId;
    private Integer day;
    private Integer order;
}
