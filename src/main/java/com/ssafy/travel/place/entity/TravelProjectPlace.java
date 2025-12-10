package com.ssafy.travel.place.entity;

import lombok.Data;

@Data
public class TravelProjectPlace {
    private Long placeId;
    private Long projectId;
    private String googlePlaceId;
    private String placeName;
    private String placeAddress;
    private Double latitude;
    private Double longitude;
    private String thumbnail;
}
