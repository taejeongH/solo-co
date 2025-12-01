package com.ssafy.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class ItineraryApplyRequestDto {
    private List<DayPlan> days;

    @Data
    public static class DayPlan {
        private int day;
        private List<String> places;
    }
}