package com.ssafy.place.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PlaceSearchResponseDto {
    private List<PlaceSearchItemDto> places;
    private String nextPageToken;
}
