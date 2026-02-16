package com.ssafy.travel.itinerary.dto;

import lombok.Data;

@Data
public class ItineraryItemDto {
    private Long placeId;
    private String googlePlaceId;
    private Integer day;
    private Integer order;
}
