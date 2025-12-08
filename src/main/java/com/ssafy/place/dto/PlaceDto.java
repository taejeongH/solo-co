package com.ssafy.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PlaceDto {
    private String placeId;
    private String name;
    private String formattedAddress;
    private String formattedPhoneNumber;
    private List<String> types; // Categories
    private List<String> photoUrls; // Full URLs to the place photos (constructed by backend)
    // For more details, like opening_hours, website, rating, etc., would be added for detailed view
}
