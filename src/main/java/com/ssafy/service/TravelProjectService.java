package com.ssafy.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.dto.response.TravelProjectResponseDto;
import com.ssafy.entity.TravelProject;
import com.ssafy.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectService {

    private final TravelProjectMapper projectMapper;

    public List<TravelProjectResponseDto> getMyProjects(Long userId) {

        List<TravelProject> projects = projectMapper.findProjectsByUserId(userId);
        List<TravelProjectResponseDto> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        for (TravelProject p : projects) {
        	System.out.println(p.toString());
            LocalDate start = LocalDate.parse(p.getStartDate(), formatter);
            LocalDate end = LocalDate.parse(p.getEndDate(), formatter);

            TravelProjectResponseDto dto = new TravelProjectResponseDto();
            dto.setProjectId(p.getProjectId());
            dto.setTitle(p.getTitle());
            dto.setStartDate(p.getStartDate());
            dto.setEndDate(p.getEndDate());
            dto.setThumbnail(p.getThumbnail());
            dto.setLocation(p.getLocation());
            dto.setProjectType(p.getProjectType());

            if (today.isBefore(start)) {
                dto.setStatus("UPCOMING");  // 예정
            } else if (!today.isAfter(end)) {
                dto.setStatus("IN_PROGRESS"); // 진행중
            } else {
                dto.setStatus("DONE"); // 종료
            }

            result.add(dto);
        }

        return result;
    }

}
