package com.ssafy.travel.community.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.travel.community.dto.request.CommentCreateRequestDto;
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
            throw new RuntimeException("존재하지 않는 프로젝트입니다.");
        }
        
        //멤버 권한 체크
        if (!memberMapper.isMember(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로젝트 접근 권한이 없습니다.");
        }
        
        //게시글 존재 여부 확인
        if (postMapper.findPostAuthorId(postId) == null) {
            throw new RuntimeException("존재하지 않는 게시글입니다.");
        }
    }
    
    public ProjectPostComment createComment(Long projectId, Long postId, Long userId, CommentCreateRequestDto req) {
    	checkPermission(projectId, userId, postId);
        
        //댓글 등록
        postCommentMapper.insertComment(postId, userId, req.getContent());

        //방금 삽입된 commentId 조회
        Long commentId = postCommentMapper.getLastInsertId();

        //작성한 댓글 조회해서 반환
        return postCommentMapper.findCommentByCommentId(commentId);
    }
}
