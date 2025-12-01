package com.ssafy.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dto.request.ItineraryApplyRequestDto;
import com.ssafy.dto.response.AutoGenerateResponse;
import com.ssafy.dto.response.ItineraryCandidateResponse;
import com.ssafy.entity.TravelProject;
import com.ssafy.entity.TravelProjectPlace;
import com.ssafy.mapper.TravelItineraryMapper;
import com.ssafy.mapper.TravelProjectMapper;
import com.ssafy.mapper.TravelProjectPlaceMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelItineraryService {

    private final TravelProjectPlaceMapper placeMapper;
	private final TravelProjectMapper projectMapper;
    private final AIService aiService;
    private final TravelItineraryMapper itineraryMapper;

    public AutoGenerateResponse autoGenerate(Long projectId) {

        // 1. 프로젝트 조회
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) throw new RuntimeException("잘못된 프로젝트 ID");

        // 2. 여행 일수 계산
        int tripDays = calculateTripDays(project);

        // 3. 장소 조회
        List<TravelProjectPlace> places = placeMapper.findByProjectId(projectId);
        if (places.isEmpty()) {
            throw new RuntimeException("장소를 먼저 추가하세요.");
        }

        // 4. AI 추천 생성 (🔥 tripDays도 전달)
        List<ItineraryCandidateResponse> candidates =
                aiService.generateItineraryCandidates(tripDays, places);

        // 5. 응답 구성
        AutoGenerateResponse response = new AutoGenerateResponse();
        response.setProjectId(projectId);
        response.setCandidates(candidates);

        return response;
    }


    @Transactional
    public void applySelectedCandidate(Long projectId, ItineraryApplyRequestDto request) {
        
        // 1. 기존 일정 삭제
        itineraryMapper.deleteByProjectId(projectId);

        // 2. day별 장소 row 저장
        for (ItineraryApplyRequestDto.DayPlan dayPlan : request.getDays()) {

            itineraryMapper.insertItinerary(
                    projectId,
                    dayPlan.getDay(),
                    dayPlan.getPlaces()   // List<String>
            );
        }
    }
    
    private int calculateTripDays(TravelProject project) {
        LocalDate start = LocalDate.parse(project.getStartDate());
        LocalDate end = LocalDate.parse(project.getEndDate());
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }
}
