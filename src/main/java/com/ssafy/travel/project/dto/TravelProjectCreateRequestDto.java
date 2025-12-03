package com.ssafy.travel.project.dto;

import lombok.Data;

@Data
public class TravelProjectCreateRequestDto {
    private String title;
    private String location;
    private String startDate;   // "yyyy-MM-dd"
    private String endDate;
    private String projectType; // SOLO / GROUP
}
