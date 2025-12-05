package com.ssafy.travel.community.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreatePostRequestDto {
    private String title;
    private String content;
    private List<String> tags;
    private VoteCreateRequestDto vote;
    
}
