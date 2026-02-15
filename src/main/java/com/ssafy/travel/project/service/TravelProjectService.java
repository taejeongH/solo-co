package com.ssafy.travel.project.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.global.service.S3Service;
import com.ssafy.travel.itinerary.mapper.TravelItineraryMapper;
import com.ssafy.travel.place.mapper.TravelProjectPlaceMapper;
import com.ssafy.travel.project.dto.TravelProjectDetailResponseDto;
import com.ssafy.travel.project.dto.TravelProjectMemberResponseDto;
import com.ssafy.travel.project.dto.TravelProjectRequestDto;
import com.ssafy.travel.project.dto.TravelProjectResponseDto;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.entity.TravelProjectMember;
import com.ssafy.travel.project.mapper.TravelProjectInviteMapper;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectService {

    private final TravelProjectMapper projectMapper;
    private final TravelProjectMemberMapper projectMemberMapper;
    private final TravelProjectInviteMapper inviteMapper;
    private final TravelItineraryMapper itineraryMapper;
    private final TravelProjectPlaceMapper placeMapper;
    private final S3Service s3service;
    private final ProjectEventService eventService;

    public String calStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(start))
            return "UPCOMING";
        else if (!today.isAfter(end))
            return "IN_PROGRESS";
        else
            return "DONE";
    }

    public List<TravelProjectResponseDto> getMyProjects(Long userId, String projectType) {

        List<TravelProject> projects = projectMapper.findProjectsByUserIdAndType(userId, projectType);
        List<TravelProjectResponseDto> result = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TravelProject p : projects) {
            LocalDate start = LocalDate.parse(p.getStartDate(), formatter);
            LocalDate end = LocalDate.parse(p.getEndDate(), formatter);
            String status = calStatus(start, end);

            TravelProjectResponseDto dto = new TravelProjectResponseDto();
            dto.setProjectId(p.getProjectId());
            dto.setTitle(p.getTitle());
            dto.setStartDate(p.getStartDate());
            dto.setEndDate(p.getEndDate());
            dto.setThumbnail(s3service.generatePresignedUrl(p.getThumbnail()));
            dto.setLocation(p.getLocation());
            dto.setProjectType(p.getProjectType());
            dto.setStatus(status);
            dto.setMemberCount(projectMemberMapper.countMembersByProjectId(p.getProjectId()));

            result.add(dto);
        }
        return result;
    }

    public TravelProjectResponseDto createProject(Long userId,
            TravelProjectRequestDto req, MultipartFile file) throws IOException {
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = s3service.upload(file, "profile");
        }

        // TravelProject 도메인 생성
        TravelProject project = new TravelProject();
        project.setOwnerId(userId);
        project.setTitle(req.getTitle());
        project.setLocation(req.getLocation());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        project.setProjectType(req.getProjectType());
        project.setThumbnail(imageUrl);

        // DB 저장 (projectId 자동 세팅됨)
        projectMapper.createProject(project);
        projectMemberMapper.insertMember(project.getProjectId(), userId, "OWNER");

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
        res.setThumbnail(s3service.generatePresignedUrl(project.getThumbnail()));
        res.setStatus(status);

        return res;
    }

    @Transactional
    public TravelProjectResponseDto updateProject(
            Long projectId,
            Long userId,
            TravelProjectRequestDto dto,
            MultipartFile file) throws IOException {

        // 1. 기존 프로젝트 조회
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 권한 체크
        if (!project.getOwnerId().equals(userId)) {
            throw new CustomException(ErrorCode.PROJECT_OWNER_ONLY);
        }

        // 3. 새로운 썸네일 파일 업로드
        String thumbnailUrl = project.getThumbnail();
        if (file != null && !file.isEmpty()) {
            thumbnailUrl = s3service.upload(file, "project");
        }

        // 4. DB 업데이트
        project.setTitle(dto.getTitle());
        project.setLocation(dto.getLocation());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setProjectType(dto.getProjectType());
        project.setThumbnail(thumbnailUrl);

        projectMapper.update(project);

        // 5. 알림 전송
        eventService.notifyProjectUpdate(projectId, "PROJECT_UPDATED");

        // 6. Response 생성
        TravelProjectResponseDto res = new TravelProjectResponseDto();
        res.setProjectId(project.getProjectId());
        res.setTitle(project.getTitle());
        res.setLocation(project.getLocation());
        res.setStartDate(project.getStartDate());
        res.setEndDate(project.getEndDate());
        res.setProjectType(project.getProjectType());
        res.setThumbnail(s3service.generatePresignedUrl(project.getThumbnail()));
        res.setStatus(project.getStatus());

        return res;
    }

    @Transactional
    public void deleteProject(Long projectId, Long requesterId) {

        // 1) 요청자가 프로젝트 멤버인지 확인
        TravelProjectMember requester = projectMemberMapper.findOne(projectId, requesterId);
        if (requester == null) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 2) OWNER인지 확인
        if (!"OWNER".equals(requester.getRole())) {
            throw new CustomException(ErrorCode.PROJECT_OWNER_ONLY);
        }

        // 3) 순서대로 관련 데이터 제거
        inviteMapper.deleteByProjectId(projectId);
        projectMemberMapper.deleteAllByProjectId(projectId);
        itineraryMapper.deleteByProjectId(projectId);
        placeMapper.deleteAllByProjectId(projectId);

        // 4) 마지막으로 프로젝트 삭제
        projectMapper.delete(projectId);

        // 5) 알림 전송
        eventService.notifyProjectUpdate(projectId, "PROJECT_DELETED");
    }

    public TravelProjectDetailResponseDto getProjectDetail(Long userId, Long projectId) {
        validate(userId, projectId);
        List<TravelProjectMemberResponseDto> members = projectMemberMapper.findMembers(projectId);
        members.forEach(m -> {
            if (m.getProfileImage() != null) {
                m.setProfileImage(s3service.generatePresignedUrl(m.getProfileImage()));
            }
        });
        TravelProject p = projectMapper.findById(projectId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(p.getStartDate(), formatter);
        LocalDate end = LocalDate.parse(p.getEndDate(), formatter);
        String status = calStatus(start, end);

        TravelProjectResponseDto dto = new TravelProjectResponseDto();
        dto.setProjectId(p.getProjectId());
        dto.setTitle(p.getTitle());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setThumbnail(s3service.generatePresignedUrl(p.getThumbnail()));
        dto.setLocation(p.getLocation());
        dto.setProjectType(p.getProjectType());
        dto.setStatus(status);
        dto.setMemberCount(members.size());

        TravelProjectDetailResponseDto response = new TravelProjectDetailResponseDto();
        response.setMembers(members);
        response.setProject(dto);

        return response;
    }

    public void validate(Long userId, Long projectId) {
        // 1. 프로젝트 유효성 체크
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 멤버 권한 체크
        if (!projectMemberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}
