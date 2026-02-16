package com.ssafy.place.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersonalPlaceDto {

    private String placeId;
    private String name;
    private String formattedAddress;

    private double rating; // 평점
    private int soloDifficulty; // 혼밥 난이도 (0~100)
    private String scoreJustification; // AI의 점수 산출 근거
    private List<String> tags; // 키오스크, 1인석, 빠른 주문 등

    private List<String> types;
    private List<String> photoUrls;

    private double lat;
    private double lng;
}
