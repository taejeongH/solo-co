package com.ssafy.travel.place.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectPlaceListResponseDto {
    private Long placeId;
    private String placeName;
    private String placeAddress;
    private Double latitude;
    private Double longitude;
    private String googlePlaceId;
    private String thumbnail;
    private String placeType;
    private java.time.LocalDateTime createdAt;
}
