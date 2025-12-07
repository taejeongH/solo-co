package com.ssafy.travel.community.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;
import com.ssafy.travel.community.entity.ProjectPostVoteResult;
import com.ssafy.travel.community.mapper.ProjectPostMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteOptionMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteResultMapper;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityVoteService {

    private final TravelProjectMapper projectMapper;
    private final ProjectPostMapper postMapper;
    private final TravelProjectMemberMapper memberMapper;
    private final ProjectPostVoteOptionMapper voteOptionMapper;
    private final ProjectPostVoteResultMapper voteResultMapper;

    void checkPermission(Long projectId, Long userId, Long postId) {
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        if (!memberMapper.isMember(projectId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        
        if (postMapper.findPostAuthorId(postId) == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Transactional
    public void castVote(Long projectId, Long postId, Long userId, Long optionId) {
        // 1. 권한 확인
        checkPermission(projectId, userId, postId);

        // 2. 투표 옵션 확인 및 voteId 가져오기
        ProjectPostVoteOption option = voteOptionMapper.findOptionById(optionId);
        System.out.println(optionId);
        if (option == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 투표 항목입니다.");
        }
        Long voteId = option.getVoteId();

        // 3. 중복 투표 확인
        ProjectPostVoteResult existingVote = voteResultMapper.findVoteByUser(voteId, userId);
        if (existingVote != null) {
            throw new CustomException(ErrorCode.VOTE_ALREADY_EXISTS);
        }

        // 4. 투표 결과 저장
        voteResultMapper.insertVoteResult(voteId, userId, optionId);
    }
}
