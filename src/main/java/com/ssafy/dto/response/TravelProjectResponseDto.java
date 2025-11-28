package com.ssafy.dto.response;

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
}
