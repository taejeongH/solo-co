package com.ssafy.travel.project.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.travel.project.dto.InviteLinkResponseDto;
import com.ssafy.travel.project.dto.InviteValidationResponseDto;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.entity.TravelProjectInvite;
import com.ssafy.travel.project.mapper.TravelProjectInviteMapper;
import com.ssafy.travel.project.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectInviteService {

    private final TravelProjectInviteMapper inviteMapper;
    private final TravelProjectMapper projectMapper;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Transactional
    public InviteLinkResponseDto createInviteLink(Long projectId, Long userId) {

    	//project가 존재한지 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new RuntimeException("존재하지 않는 프로젝트입니다.");
        }

        //project 소유자인지 확인
        if (!project.getOwnerId().equals(userId)) {
            throw new RuntimeException("프로젝트 소유자만 초대 링크를 생성할 수 있습니다.");
        }
        
        //초대 링크가 이미 존재하는 지 확인
        TravelProjectInvite existing = inviteMapper.findByProjectId(projectId);
        if (existing != null) {

            // 만료된 경우만 새로 생성
            if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
                // 기존 초대 무효 처리 (optional)
                inviteMapper.deleteById(existing.getInviteId());
            } else {
                // 유효하면 기존 값 반환
            	InviteLinkResponseDto dto = new InviteLinkResponseDto();
                dto.setProjectId(existing.getProjectId());
                dto.setInviteCode(existing.getInviteCode());
                dto.setInviteUrl(frontendBaseUrl + "/invite/" + existing.getInviteCode());
                return dto;
            }
        }
        
        
        // 2) 초대 코드 생성
        String code = UUID.randomUUID().toString().replace("-", "");

        TravelProjectInvite invite = new TravelProjectInvite();
        invite.setProjectId(projectId);
        invite.setInviteCode(code);
        invite.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7일 유효
        invite.setMaxUses(null);   // 제한 없으면 null
        invite.setUseCount(0);

        inviteMapper.insert(invite);

        // 3) 응답 DTO 생성
        InviteLinkResponseDto dto = new InviteLinkResponseDto();
        dto.setProjectId(projectId);
        dto.setInviteCode(code);
        dto.setInviteUrl(frontendBaseUrl + "/invite/" + code);

        return dto;
    }
    
    public InviteValidationResponseDto validateInvite(String code) {

        TravelProjectInvite invite = inviteMapper.findByCode(code);
        if (invite == null) {
            throw new RuntimeException("유효하지 않은 초대 코드입니다.");
        }

        TravelProject project = projectMapper.findById(invite.getProjectId());

        InviteValidationResponseDto res = new InviteValidationResponseDto();
        res.setValid(true);
        res.setProjectId(project.getProjectId());
        res.setProjectTitle(project.getTitle());
        res.setLocation(project.getLocation());

        return res;
    }

}
