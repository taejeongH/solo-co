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
import com.ssafy.travel.community.dto.ProjectPostDetailResponseDto;
import com.ssafy.travel.community.dto.ProjectPostListResponseDto;
import com.ssafy.travel.community.dto.VoteCreateRequestDto;
import com.ssafy.travel.community.dto.VoteDetailDto;
import com.ssafy.travel.community.dto.VoteOptionDto;
import com.ssafy.travel.community.entity.ProjectPost;
import com.ssafy.travel.community.entity.ProjectPostVote;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;
import com.ssafy.travel.community.mapper.PostCommentMapper;
import com.ssafy.travel.community.mapper.PostVoteOptionMapper;
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
    private final PostVoteOptionMapper voteOptionMapper;
    private final PostCommentMapper commentMapper;
    private final S3Service s3Service;
    
    void checkPermission(Long projectId, Long userId) {
    	//project가 존재한지 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
            throw new RuntimeException("존재하지 않는 프로젝트입니다.");
        }
        
        //멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 접근 권한이 없습니다.");
        }
    }
    
    @Transactional
    public Long createPost(Long projectId, Long userId,
                           CreatePostRequestDto dto,
                           List<MultipartFile> images) throws IOException {
    	
    	checkPermission(projectId, userId);
    	
    	
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
    

    
    public List<ProjectPostListResponseDto> getPostList(Long projectId, Long userId) {
    	checkPermission(projectId, userId);
        
    	List<ProjectPostListResponseDto> posts = postMapper.findAllPostsByProjectId(projectId);
        for (ProjectPostListResponseDto post : posts) {
            post.setImages(imageMapper.findImagesByPostId(post.getPostId()));
            post.setTags(tagMapper.findTagsByPostId(post.getPostId()));
        }

        return posts;
    }
    
    public ProjectPostDetailResponseDto getPostDetail(Long projectId, Long postId, Long userId) {
    	checkPermission(projectId, userId);
        // 게시글 기본 정보
    	ProjectPostDetailResponseDto post = postMapper.findPostDetail(postId);

        // 이미지
        post.setImages(imageMapper.findImagesByPostId(postId));

        // 태그
        post.setTags(tagMapper.findTagsByPostId(postId));

        // 투표
        VoteDetailDto vote = voteMapper.findVoteByPostId(postId);

        if (vote != null) {
            List<VoteOptionDto> options = voteOptionMapper.findVoteOptions(vote.getVoteId());
            vote.setOptions(options);

            int total = options.stream()
                    .mapToInt(VoteOptionDto::getCount)
                    .sum();
            vote.setTotalVotes(total);

            Boolean hasVoted = voteMapper.hasUserVoted(vote.getVoteId(), userId);
            vote.setHasVoted(hasVoted);
        }

        post.setVote(vote);

        // 댓글
        post.setComments(commentMapper.findCommentsByPostId(postId));

        return post;
    }

}
