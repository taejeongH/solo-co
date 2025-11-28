package com.ssafy.entity;

import lombok.Data;

@Data
public class TravelProject {
    private Long projectId;
    private Long ownerId;
    private String projectType;   // PERSONAL / GROUP
    private String title;
    private String location;
    private String startDate;
    private String endDate;
    private String thumbnail;
    private String createdAt;
}