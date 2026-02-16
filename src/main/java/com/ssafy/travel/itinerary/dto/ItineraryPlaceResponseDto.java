package com.ssafy.travel.itinerary.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItineraryPlaceResponseDto {
    private Integer order;
    private Long placeId;
    private String placeName;
    private String address;
    private String placeType;
    private Double latitude;
    private Double longitude;
    private String thumbnail;
    @JsonIgnore
    private Integer day;
}
