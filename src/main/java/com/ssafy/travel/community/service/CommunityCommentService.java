package com.ssafy.travel.community.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.community.dto.request.CommentCreateRequestDto;
import com.ssafy.travel.community.dto.request.CommentUpdateRequestDto;
import com.ssafy.travel.community.entity.ProjectPostComment;
import com.ssafy.travel.community.mapper.ProjectPostCommentMapper;
import com.ssafy.travel.community.mapper.ProjectPostMapper;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;
import com.ssafy.travel.project.mapper.TravelProjectMemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {
	
	private final TravelProjectMapper projectMapper;
    private final ProjectPostMapper postMapper;
    private final ProjectPostCommentMapper postCommentMapper;
    private final TravelProjectMemberMapper memberMapper;
    
    void checkPermission(Long projectId, Long userId, Long postId) {
    	//project가 존재한지 확인
        TravelProject project = projectMapper.findById(projectId);
        if (project == null) {
        	throw new CustomException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        //멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
        	throw new CustomException(ErrorCode.FORBIDDEN);
        }
        
        //게시글 존재 여부 확인
        if (postMapper.findPostAuthorId(postId) == null) {
        	throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }
    
    @Transactional
    public ProjectPostComment createComment(Long projectId, Long postId, Long userId, CommentCreateRequestDto req) {
    	checkPermission(projectId, userId, postId);
        
        //댓글 등록
        postCommentMapper.insertComment(postId, userId, req.getContent());

        //방금 삽입된 commentId 조회
        Long commentId = postCommentMapper.getLastInsertId();

        //작성한 댓글 조회해서 반환
        return postCommentMapper.findCommentByCommentId(commentId);
    }
    
    @Transactional
    public ProjectPostComment updateComment(Long projectId, Long postId, Long commentId, Long userId, CommentUpdateRequestDto req) {
        // 0. 프로젝트, 게시글, 멤버십 권한 확인
        checkPermission(projectId, userId, postId);

        // 1. 댓글 조회
        ProjectPostComment comment = postCommentMapper.findCommentByCommentId(commentId);
        if (comment == null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 2. 권한 확인 (본인만 수정 가능)
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "댓글을 수정할 권한이 없습니다.");
        }

        // 3. 댓글 수정
        postCommentMapper.updateComment(commentId, req.getContent());

        // 4. 수정된 댓글 정보 반환
        return postCommentMapper.findCommentByCommentId(commentId);
    }
}
