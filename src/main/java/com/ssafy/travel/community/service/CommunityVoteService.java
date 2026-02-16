package com.ssafy.travel.community.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.community.dto.response.VoteOptionResultDto;
import com.ssafy.travel.community.dto.response.VoteResultDto;
import com.ssafy.travel.community.entity.ProjectPostVote;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;
import com.ssafy.travel.community.entity.ProjectPostVoteResult;
import com.ssafy.travel.community.mapper.ProjectPostMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteMapper;
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
    private final ProjectPostVoteMapper voteMapper;
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
        try {
            voteResultMapper.insertVoteResult(voteId, userId, optionId);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new CustomException(ErrorCode.VOTE_ALREADY_EXISTS);
        }
    }

    public VoteResultDto getVoteResult(Long projectId, Long postId, Long userId) {

        checkPermission(projectId, userId, postId);

        // 1. 투표 정보 가져오기
        ProjectPostVote vote = voteMapper.findVoteEntityByPostId(postId);
        if (vote == null) {
            throw new CustomException(ErrorCode.VOTE_NOT_FOUND);
        }
        Long voteId = vote.getVoteId();

        // 2. 전체 투표 수 계산
        int totalVotes = voteResultMapper.countTotalVotes(voteId);

        // 3. 각 옵션별 투표 수 계산
        List<VoteOptionResultDto> optionResults = voteOptionMapper.findOptionsByVoteId(voteId).stream()
                .map((ProjectPostVoteOption option) -> {
                    int voteCount = voteResultMapper.countVotesByOption(option.getOptionId());
                    return VoteOptionResultDto.builder()
                            .optionId(option.getOptionId())
                            .content(option.getOptionText())
                            .voteCount(voteCount)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 결과 DTO 생성
        return VoteResultDto.builder()
                .totalVotes(totalVotes)
                .options(optionResults)
                .build();
    }

    @Transactional
    public void cancelVote(Long projectId, Long postId, Long userId) {
        // 1. 권한 확인
        checkPermission(projectId, userId, postId);

        // 2. 투표 정보 가져오기
        ProjectPostVote vote = voteMapper.findVoteEntityByPostId(postId);
        if (vote == null) {
            throw new CustomException(ErrorCode.VOTE_NOT_FOUND);
        }
        Long voteId = vote.getVoteId();

        // 3. 사용자의 투표 결과 삭제
        int deletedRows = voteResultMapper.deleteVoteByUser(voteId, userId);
        if (deletedRows == 0) {
            throw new CustomException(ErrorCode.VOTE_NOT_FOUND, "해당 사용자의 투표 기록을 찾을 수 없습니다.");
        }
    }
}
