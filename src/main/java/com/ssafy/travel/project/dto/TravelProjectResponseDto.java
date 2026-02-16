package com.ssafy.travel.project.dto;

import lombok.Data;

@Data
public class TravelProjectResponseDto {
    private Long projectId;
    private String projectType;
    private String title;
    private String location;
    private String startDate;
    private String endDate;
    private String thumbnail;
    private String status; // 여행 상태
    private int memberCount;
}
