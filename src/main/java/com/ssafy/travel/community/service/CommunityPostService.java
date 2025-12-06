package com.ssafy.travel.community.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.global.service.S3Service;
import com.ssafy.travel.community.dto.request.CreatePostRequestDto;
import com.ssafy.travel.community.dto.request.VoteCreateRequestDto;
import com.ssafy.travel.community.dto.response.ProjectPostDetailResponseDto;
import com.ssafy.travel.community.dto.response.ProjectPostListResponseDto;
import com.ssafy.travel.community.dto.response.VoteDetailResponseDto;
import com.ssafy.travel.community.dto.response.VoteOptionResponseDto;
import com.ssafy.travel.community.entity.ProjectPost;
import com.ssafy.travel.community.entity.ProjectPostVote;
import com.ssafy.travel.community.entity.ProjectPostVoteOption;
import com.ssafy.travel.community.mapper.ProjectPostCommentMapper;
import com.ssafy.travel.community.mapper.ProjectPostImageMapper;
import com.ssafy.travel.community.mapper.ProjectPostMapper;
import com.ssafy.travel.community.mapper.ProjectPostTagMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteOptionMapper;
import com.ssafy.travel.community.mapper.ProjectPostVoteResultMapper;
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
    private final ProjectPostVoteOptionMapper voteOptionMapper;
    private final ProjectPostVoteResultMapper voteResultMapper;
    private final ProjectPostCommentMapper commentMapper;
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

        post.setImages(imageMapper.findImagesByPostId(postId));
        post.setTags(tagMapper.findTagsByPostId(postId));
        VoteDetailResponseDto vote = voteMapper.findVoteByPostId(postId);

        if (vote != null) {
            List<VoteOptionResponseDto> options = voteOptionMapper.findVoteOptions(vote.getVoteId());
            vote.setOptions(options);

            int total = options.stream()
                    .mapToInt(VoteOptionResponseDto::getCount)
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
    
    public void deletePost(Long postId, Long userId) {

        // 게시글 존재 & 작성자 확인
    	Long authorId = postMapper.findPostAuthorId(postId);
        if (authorId == null)
            throw new RuntimeException("존재하지 않는 게시글입니다.");

        if (!authorId.equals(userId))
            throw new RuntimeException("본인이 작성한 게시글만 삭제할 수 있습니다.");

        commentMapper.deleteCommentsByPostId(postId);
        tagMapper.deleteTagsByPostId(postId);
        imageMapper.deleteImagesByPostId(postId);

        // 투표 관련 삭제
        Long voteId = voteMapper.findVoteIdByPostId(postId);
        if (voteId != null) {
            voteResultMapper.deleteResultsByVoteId(voteId);
            voteOptionMapper.deleteOptionsByVoteId(voteId);
            voteMapper.deleteVote(voteId);
        }

        // 게시글 삭제
        postMapper.deletePost(postId);
    }
    
    public void updatePost(Long projectId, Long postId, Long userId, CreatePostRequestDto request, List<MultipartFile> images) throws IOException {

        // 작성자 검증
    	checkPermission(projectId, userId);
        Long authorId = postMapper.findPostAuthorId(postId);
        if (authorId == null)
            throw new RuntimeException("존재하지 않는 게시글입니다.");

        if (!authorId.equals(userId))
            throw new RuntimeException("본인만 게시글을 수정할 수 있습니다.");

        // 2️⃣ 게시글 기본 정보 업데이트
        postMapper.updatePostBasic(postId, request.getTitle(), request.getContent());

        // 이미지 전체 교체
        imageMapper.deleteImagesByPostId(postId);
        if (images != null && images.size() > 0)  {
            int order = 1;
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;
                String imageUrl = s3Service.upload(file, "community/posts");
                imageMapper.insertImage(postId, imageUrl, order++);
            }
        }

        // 태그 전체 교체
        tagMapper.deleteTagsByPostId(postId);
        if (request.getTags() != null) {
            for (String tag : request.getTags()) {
                tagMapper.insertTag(postId, tag);
            }
        }

        // 5️⃣ 투표 전체 교체 (없으면 삭제)
        updateVote(postId, request.getVote());
    }
    private boolean isSameVote(VoteDetailResponseDto oldVote, VoteCreateRequestDto newVote) {

        if (oldVote == null && newVote == null) return true;
        if (oldVote == null || newVote == null) return false;

        if (!oldVote.getQuestion().equals(newVote.getQuestion())) return false;
        // 옵션 길이 다르면 다름
        List<ProjectPostVoteOption> oldOptions = voteMapper.findOptionsByVoteId(oldVote.getVoteId());
        List<String> newOptions = newVote.getOptions();
        if (oldOptions.size() != newOptions.size()) return false;

        // 옵션 내용 하나라도 다르면 다름
        for (int i = 0; i < oldOptions.size(); i++) {
            if (!oldOptions.get(i).getOptionText().equals(newOptions.get(i))) {
                return false;
            }
        }

        // 전부 동일
        return true;
    }

    private void updateVote(Long postId, VoteCreateRequestDto vote) {

        // 기존 voteId 조회
        Long voteId = voteMapper.findVoteIdByPostId(postId);
        VoteDetailResponseDto oldVote = voteMapper.findVoteByPostId(postId);
        
        if (isSameVote(oldVote, vote)) {
            return;
        }
        
        // 기존 투표가 있다면 삭제
        if (voteId != null) {
            voteResultMapper.deleteResultsByVoteId(voteId);
            voteOptionMapper.deleteOptionsByVoteId(voteId);
            voteMapper.deleteVote(voteId);
        }
        
        if (vote != null) {
            //투표 마스터 저장
            ProjectPostVote voteEntity = new ProjectPostVote();
            voteEntity.setPostId(postId);
            voteEntity.setQuestion(vote.getQuestion());
            voteEntity.setMultipleChoice(vote.getMultipleChoice() != null ? vote.getMultipleChoice() : false);
            voteMapper.insertVote(voteEntity);

            Long newvoteId = voteEntity.getVoteId();

            // 투표 옵션 저장
            int order = 1;
            for (String option : vote.getOptions()) {
                ProjectPostVoteOption optionEntity = new ProjectPostVoteOption();
                optionEntity.setVoteId(newvoteId);
                optionEntity.setOptionText(option);
                optionEntity.setOrderNo(order++);
                voteMapper.insertOption(optionEntity);
            }
        }
        

        
    }

}
