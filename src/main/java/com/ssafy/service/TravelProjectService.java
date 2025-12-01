package com.ssafy.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dto.request.TravelProjectPlaceRequestDto;
import com.ssafy.dto.request.TravelProjectCreateRequestDto;
import com.ssafy.dto.response.AutoGenerateResponse;
import com.ssafy.dto.response.ItineraryCandidateResponse;
import com.ssafy.dto.response.TravelProjectResponseDto;
import com.ssafy.entity.TravelProject;
import com.ssafy.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectService {

    private final TravelProjectMapper projectMapper;
    
    public String calStatus (LocalDate start, LocalDate end) {
    	LocalDate today = LocalDate.now();
    	
    	if(today.isBefore(start)) return "UPCOMING";
    	else if(!today.isAfter(end)) return "IN_PROGRESS";
    	else return "DONE";
    }

    public List<TravelProjectResponseDto> getMyProjects(Long userId) {

        List<TravelProject> projects = projectMapper.findProjectsByUserId(userId);
        List<TravelProjectResponseDto> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TravelProject p : projects) {
        	System.out.println(p.toString());
            LocalDate start = LocalDate.parse(p.getStartDate(), formatter);
            LocalDate end = LocalDate.parse(p.getEndDate(), formatter);
            String status = calStatus(start, end);

            TravelProjectResponseDto dto = new TravelProjectResponseDto();
            dto.setProjectId(p.getProjectId());
            dto.setTitle(p.getTitle());
            dto.setStartDate(p.getStartDate());
            dto.setEndDate(p.getEndDate());
            dto.setThumbnail(p.getThumbnail());
            dto.setLocation(p.getLocation());
            dto.setProjectType(p.getProjectType());
            dto.setStatus(status);
            
            result.add(dto);
        }
        return result;
    }
    
    public TravelProjectResponseDto createProject(Long userId,
            TravelProjectCreateRequestDto req) {

		// TravelProject 도메인 생성
		TravelProject project = new TravelProject();
		project.setOwnerId(userId);
		project.setTitle(req.getTitle());
		project.setLocation(req.getLocation());
		project.setStartDate(req.getStartDate());
		project.setEndDate(req.getEndDate());
		project.setProjectType(req.getProjectType());
		project.setThumbnail(req.getThumbnail());
		
		// DB 저장 (projectId 자동 세팅됨)
		projectMapper.createProject(project);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate start = LocalDate.parse(project.getStartDate(), formatter);
        LocalDate end = LocalDate.parse(project.getEndDate(), formatter);
        String status = calStatus(start, end);
		
		// 응답 DTO 생성
		TravelProjectResponseDto res = new TravelProjectResponseDto();
		res.setProjectId(project.getProjectId());
		res.setTitle(project.getTitle());
		res.setLocation(project.getLocation());
		res.setStartDate(project.getStartDate());
		res.setEndDate(project.getEndDate());
		res.setProjectType(project.getProjectType());
		res.setThumbnail(project.getThumbnail());
        res.setStatus(status);
		
		return res;
	}
    
}
