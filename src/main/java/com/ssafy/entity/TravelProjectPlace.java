package com.ssafy.entity;

import lombok.Data;

@Data
public class TravelProjectPlace {
    private Long placeId;
    private Long projectId;
    private String placeName;
    private String placeAddress;
    private Double latitude;
    private Double longitude;
}
