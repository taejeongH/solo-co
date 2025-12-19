package com.ssafy.place.ai.dto;

import java.util.List;

import lombok.Data;

@Data
public class SoloPlaceAnalysisDto {
    private int soloDifficultyScore;
    private List<String> tags;
    private String scoreJustification;
}
