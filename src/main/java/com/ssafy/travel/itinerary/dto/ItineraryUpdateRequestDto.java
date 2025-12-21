package com.ssafy.travel.itinerary.dto;

import java.util.List;
import lombok.Data;

@Data
public class ItineraryUpdateRequestDto {
    private List<ItineraryItemDto> places;
}
