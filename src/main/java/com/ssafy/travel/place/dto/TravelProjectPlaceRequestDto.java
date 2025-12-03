package com.ssafy.travel.place.dto;

import java.util.List;

import lombok.Data;

@Data
public class TravelProjectPlaceRequestDto {
	private String placeName;
    private String placeAddress;
    private Double latitude;
    private Double longitude;
}
