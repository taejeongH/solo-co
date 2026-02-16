package com.ssafy.ai.dto;

import java.util.List;

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
