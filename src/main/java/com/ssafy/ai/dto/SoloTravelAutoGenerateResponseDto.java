package com.ssafy.ai.dto;

import lombok.Data;

@Data
public class SoloTravelAutoGenerateResponseDto {

    private int totalScore; // 총점
    private int safety; // 치안
    private int transportAccessibility; // 대중교통 편의성
    private int routeSimplicity; // 동선 간결성
    private int landmarkAccessibility; // 랜드마크 접근성
    private int soloDiningDifficulty; // 혼밥 난이도
    private String summary; // 요약 문구
    private String recommendation; // 추천 문구
}
