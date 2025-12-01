package com.ssafy.dto.response;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItineraryCandidateResponse {

    private int candidateId;
    private List<DailyPlan> days;

    @Data
    @NoArgsConstructor
    public static class DailyPlan {
        private int day;
        private List<String> places;
    }
}
