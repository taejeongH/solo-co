package com.ssafy.travel.community.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.travel.community.dto.request.CommentCreateRequestDto;
import com.ssafy.travel.community.dto.request.CommentUpdateRequestDto;
import com.ssafy.travel.community.dto.request.CreatePostRequestDto;
import com.ssafy.travel.community.dto.request.VoteRequestDto;
import com.ssafy.travel.community.entity.ProjectPostComment;
import com.ssafy.travel.community.service.CommunityCommentService;
import com.ssafy.travel.community.service.CommunityPostService;
import com.ssafy.travel.community.service.CommunityVoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "JWT Auth")
@RestController
@RequestMapping("/api/travels/{projectId}/posts")
@RequiredArgsConstructor
public class ProjectCommunityController {
	
	private final CommunityPostService postService;
	private final CommunityCommentService commentService;
	private final CommunityVoteService voteService;
	
	@PostMapping
    @Operation(summary = "커뮤니티 게시글 작성")
	public ResponseEntity<?> createPost(
	        @PathVariable Long projectId,
	        @AuthenticationPrincipal CustomUserDetails user,
	        @RequestPart CreatePostRequestDto dto,         // JSON
	        @RequestPart(value = "images", required = false) List<MultipartFile> images // 파일
	) throws IOException {
	    Long postId = postService.createPost(projectId, user.getUserId(), dto, images);
	    return ResponseEntity.ok(
	    	    Map.of(
	    	        "message", "게시글 생성 완료",
	    	        "postId", postId
	    	    )
	    	);
	}
	
    @GetMapping
    @Operation(summary = "커뮤니티 게시글 목록 조회")
    public ResponseEntity<?> getPostList(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long projectId) {
        return ResponseEntity.ok(postService.getPostList(projectId, user.getUserId()));
    }
    
    
    @GetMapping("/{postId}")
    @Operation(summary = "커뮤니티 게시글 상세 조회")
    public ResponseEntity<?> getPostDetail(
    		@PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();

        return ResponseEntity.ok(
                postService.getPostDetail(projectId, postId, userId)
        );
    }
    
    @DeleteMapping("/{postId}")
    @Operation(summary = "커뮤니티 게시글 삭제")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        postService.deletePost(postId, userId);
        return ResponseEntity.ok("게시글 삭제 완료");
    }
    
    @PutMapping("/{postId}")
    @Operation(summary = "커뮤니티 게시글 수정")
    public ResponseEntity<?> updatePost(
    		@PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart CreatePostRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        postService.updatePost(projectId, postId, user.getUserId(), dto, images);
        return ResponseEntity.ok("게시글 수정 완료");
    }
    
    @PostMapping("/{postId}/comments")
    @Operation(summary = "커뮤니티 댓글 작성")
    public ResponseEntity<?> createComment(
    		@PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CommentCreateRequestDto request
    ) {
    	ProjectPostComment created = commentService.createComment(projectId, postId, user.getUserId(), request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "커뮤니티 댓글 수정")
    public ResponseEntity<?> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CommentUpdateRequestDto request
    ) {
        ProjectPostComment updated = commentService.updateComment(projectId, postId, commentId, user.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "커뮤니티 댓글 삭제")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        commentService.deleteComment(projectId, postId, commentId, user.getUserId());
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }

    @PostMapping("/{postId}/vote")
    @Operation(summary = "게시글 투표 참여")
    public ResponseEntity<?> castVote(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody VoteRequestDto request
    ) {
        voteService.castVote(projectId, postId, user.getUserId(), request.getOptionId());
        return ResponseEntity.ok("투표가 완료되었습니다.");
    }
    
    @GetMapping("/{postId}/vote/result")
    @Operation(summary = "게시글 투표 결과 조회")
    public ResponseEntity<?> getVoteResult(
    		@PathVariable Long projectId,
    		@PathVariable Long postId,
    		@AuthenticationPrincipal CustomUserDetails user
    		) {
    	return ResponseEntity.ok(voteService.getVoteResult(projectId, postId, user.getUserId()));
    }
}

