package com.ssafy.travel.community.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.global.service.S3Service;
import com.ssafy.travel.community.dto.CreatePostRequestDto;
import com.ssafy.travel.community.dto.VoteCreateRequestDto;
import com.ssafy.travel.community.entity.ProjectPost;
import com.ssafy.travel.community.entity.ProjectPostVote;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;
import com.ssafy.travel.community.mapper.ProjectPostImageMapper;
import com.ssafy.travel.community.mapper.ProjectPostMapper;
import com.ssafy.travel.community.mapper.ProjectPostTagMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteMapper;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityPostService {
	
	private final TravelProjectMapper projectMapper;
	private final TravelProjectMemberMapper memberMapper;
    private final ProjectPostMapper postMapper;
    private final ProjectPostImageMapper imageMapper;
    private final ProjectPostTagMapper tagMapper;
    private final ProjectPostVoteMapper voteMapper;
    private final S3Service s3Service;

    @Transactional
    public Long createPost(Long projectId, Long userId,
                           CreatePostRequestDto dto,
                           List<MultipartFile> images) throws IOException {
    	
    	//project가 존재한지 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new RuntimeException("존재하지 않는 프로젝트입니다.");
        }
        
        //멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 접근 권한이 없습니다.");
        }
    	
        // 1) 게시글 저장
        ProjectPost post = new ProjectPost();
        post.setProjectId(projectId);
        post.setAuthorId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        postMapper.insertPost(post);

        Long postId = post.getPostId();

        // 2) 이미지 업로드
        if (images != null) {
            int order = 1;
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;

                String imageUrl = s3Service.upload(file, "community/posts");

                imageMapper.insertImage(postId, imageUrl, order++);
            }
        }

        // 3) 태그 저장
        if (dto.getTags() != null) {
            for (String tag : dto.getTags()) {
                tagMapper.insertTag(postId, tag);
            }
        }
        
        if (dto.getVote() != null) {

            VoteCreateRequestDto vote = dto.getVote();

            // (1) 투표 마스터 저장
            ProjectPostVote voteEntity = new ProjectPostVote();
            voteEntity.setPostId(postId);
            voteEntity.setQuestion(vote.getQuestion());
            voteEntity.setMultipleChoice(vote.getMultipleChoice() != null ? vote.getMultipleChoice() : false);
            voteMapper.insertVote(voteEntity);

            Long voteId = voteEntity.getVoteId();

            // (2) 투표 옵션 저장
            int order = 1;
            for (String option : vote.getOptions()) {
                ProjectPostVoteOption optionEntity = new ProjectPostVoteOption();
                optionEntity.setVoteId(voteId);
                optionEntity.setOptionText(option);
                optionEntity.setOrderNo(order++);
                voteMapper.insertOption(optionEntity);
            }
        }

        return postId;
    }
}
