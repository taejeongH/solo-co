package com.ssafy.place.dto;

import java.util.List;
import java.util.Map;

import com.ssafy.place.dto.PlaceDetailDto.OpeningHoursDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersonalPlaceDetailDto {

    private String placeId;
    private String name;
    private String formattedAddress;
    private String formattedPhoneNumber;
    private List<OpeningHoursDto> openingHours;

    private double rating;
    private int userRatingsTotal;
    private int soloScore;     // 혼밥 난이도
    private String scoreJustification; // AI의 점수 산출 근거
    private List<String> tags;      // 자동 추출 태그

    private List<String> types;
    private List<String> photoUrls;
    private String website;
    private String url;
    private String businessStatus;

    private Map<String, Object> geometry;
}
