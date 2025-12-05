package com.ssafy.travel.community.dto;

import java.util.List;

import lombok.Data;

@Data
public class VoteCreateRequestDto {
    private String question;
    private Boolean multipleChoice;   // 복수 선택 여부
    private List<String> options;     // 항목 리스트
}
