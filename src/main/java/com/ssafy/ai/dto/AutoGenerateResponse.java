package com.ssafy.ai.dto;

import java.util.List;

import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoGenerateResponse {
	private String aiResultId;
    private Long projectId;
    private List<?> candidates;
}
