package com.ssafy.travel.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEventDto {
    private Long projectId;
    private String type;
    private Object data;
}
