package com.ssafy.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlaceSearchItemDto {
    private String placeId;
    private String name;
    private String formattedAddress;
}
