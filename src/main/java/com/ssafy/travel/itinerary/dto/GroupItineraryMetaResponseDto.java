package com.ssafy.travel.itinerary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GroupItineraryMetaResponseDto implements ItineraryMetaResponseDto {
    private String summary;
}
