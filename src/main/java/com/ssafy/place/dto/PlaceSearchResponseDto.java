package com.ssafy.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.ssafy.place.dto.PlaceSearchItemDto;

@Getter
@Setter
@Builder
public class PlaceSearchResponseDto {
    private List<PlaceSearchItemDto> places;
    private String nextPageToken; // For pagination
}
