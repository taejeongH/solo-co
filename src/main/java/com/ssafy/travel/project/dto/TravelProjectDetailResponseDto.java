package com.ssafy.travel.project.dto;

import java.util.List;

import lombok.Data;

@Data
public class TravelProjectDetailResponseDto {
	TravelProjectResponseDto project;
    List<TravelProjectMemberResponseDto> members;
}
