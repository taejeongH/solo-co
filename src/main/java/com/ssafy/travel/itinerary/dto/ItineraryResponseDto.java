package com.ssafy.travel.itinerary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ItineraryResponseDto {
    private ItineraryMetaResponseDto meta;
    private List<ItineraryDayResponseDto> days;
}
