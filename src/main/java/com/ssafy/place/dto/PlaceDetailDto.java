package com.ssafy.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class PlaceDetailDto {
    private String placeId;
    private String name;
    private String formattedAddress;
    private String formattedPhoneNumber;
    private List<String> types;
    private String website;
    private String url;
    private double rating;
    private int userRatingsTotal;
    private List<OpeningHoursDto> openingHours;
    private List<ReviewDto> reviews;
    private List<String> photoUrls;
    private Map<String, Object> geometry;
    private String businessStatus;

    @Getter
    @Setter
    @Builder
    public static class OpeningHoursDto {
        private boolean openNow;
        private List<String> weekdayText;
    }

    @Getter
    @Setter
    @Builder
    public static class ReviewDto {
        private String authorName;
        private String authorUrl;
        private String profilePhotoUrl;
        private int rating;
        private String relativeTimeDescription;
        private String text;
        private long time;
    }
}
