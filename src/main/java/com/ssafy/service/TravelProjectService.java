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
    private final JwtTokenProvider jwtTokenProvider;

    public List<TravelProjectResponseDto> getMyProjects(String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return projectMapper.findProjectsByUserId(userId);
    }
}
