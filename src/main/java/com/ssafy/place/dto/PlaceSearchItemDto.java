package com.ssafy.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PlaceSearchItemDto {
    private String placeId;
    private String name;
    private String formattedAddress;
    private String tag;
    private double lat;
    private double lng;
}
