package com.ssafy.travel.project.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.travel.project.dto.TravelProjectMemberResponseDto;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.entity.TravelProjectMember;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectMemberService {

    private final TravelProjectMemberMapper memberMapper;
    private final TravelProjectMapper projectMapper;

    public List<TravelProjectMemberResponseDto> getMembers(Long projectId, Long userId) {
        // 1. 프로젝트 유효성 체크
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다.");
        }

        // 2. 멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 접근 권한이 없습니다.");
        }
    	
    	
    	return memberMapper.findMembers(projectId);
    }

    @Transactional
    public void removeMember(Long projectId, Long memberId, Long requesterId) {

        // 1) 요청자가 해당 프로젝트의 멤버인지 확인
        TravelProjectMember requester = memberMapper.findOne(projectId, requesterId);

        if (requester == null) {
            throw new RuntimeException("해당 프로젝트의 멤버만 이 작업을 수행할 수 있습니다.");
        }

        // 2) OWNER인지 체크
        if (!"OWNER".equals(requester.getRole())) {
            throw new RuntimeException("해당 작업은 OWNER만 수행할 수 있습니다.");
        }

        // 3) OWNER는 자기 자신 삭제 불가
        if (memberId.equals(requesterId)) {
            throw new RuntimeException("OWNER는 본인을 강퇴할 수 없습니다.");
        }

        // 4) 실제 삭제
        memberMapper.deleteMember(projectId, memberId);
    }

}
