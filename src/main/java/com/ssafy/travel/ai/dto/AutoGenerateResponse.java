package com.ssafy.travel.ai.dto;

import java.util.List;

import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoGenerateResponse {
    private Long projectId;
    private List<ItineraryCandidateResponse> candidates;
}
