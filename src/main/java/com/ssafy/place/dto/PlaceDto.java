package com.ssafy.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import java.util.Map;

@Getter
@Setter
@Builder
public class PlaceDto {
    private String placeId;
    private String name;
    private String formattedAddress;
    private String formattedPhoneNumber;
    private List<String> types;
    private List<String> photoUrls;
    private Map<String, Object> geometry;
}
