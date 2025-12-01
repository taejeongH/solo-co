package com.ssafy.dto.response;

import java.util.List;

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
