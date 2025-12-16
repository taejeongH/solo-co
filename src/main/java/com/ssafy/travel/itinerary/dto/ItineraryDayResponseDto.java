package com.ssafy.travel.itinerary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ItineraryDayResponseDto {
    private Integer day;
    private List<ItineraryPlaceResponseDto> places;
}
