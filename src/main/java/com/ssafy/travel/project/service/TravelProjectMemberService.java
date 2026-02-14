package com.ssafy.travel.project.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.project.dto.TravelProjectMemberResponseDto;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.entity.TravelProjectMember;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;
import com.ssafy.global.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelProjectMemberService {

    private final TravelProjectMemberMapper memberMapper;
    private final TravelProjectMapper projectMapper;
    private final S3Service s3Service;

    public List<TravelProjectMemberResponseDto> getMembers(Long projectId, Long userId) {
        // 1. 프로젝트 유효성 체크
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<TravelProjectMemberResponseDto> members = memberMapper.findMembers(projectId);
        members.forEach(m -> {
            if (m.getProfileImage() != null) {
                m.setProfileImage(s3Service.generatePresignedUrl(m.getProfileImage()));
            }
        });
        return members;
    }

    @Transactional
    public void removeMember(Long projectId, Long memberId, Long requesterId) {

        // 1) 요청자가 해당 프로젝트의 멤버인지 확인
        TravelProjectMember requester = memberMapper.findOne(projectId, requesterId);

        if (requester == null) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 2) OWNER인지 체크
        if (!"OWNER".equals(requester.getRole())) {
            throw new CustomException(ErrorCode.PROJECT_OWNER_ONLY);
        }

        // 3) OWNER는 자기 자신 삭제 불가
        if (memberId.equals(requesterId)) {
            throw new CustomException(ErrorCode.PROJECT_OWNER_CANNOT_REMOVE_SELF);
        }

        // 4) 실제 삭제
        memberMapper.deleteMember(projectId, memberId);
    }

}
