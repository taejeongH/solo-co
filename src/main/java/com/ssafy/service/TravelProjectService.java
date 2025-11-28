package com.ssafy.service;

import com.ssafy.authentication.JwtTokenProvider;
import com.ssafy.dto.response.TravelProjectResponseDto;
import com.ssafy.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelProjectService {

    private final TravelProjectMapper projectMapper;

    public List<TravelProjectResponseDto> getMyProjects(Long userId) {
        return projectMapper.findProjectsByUserId(userId);
    }
}
