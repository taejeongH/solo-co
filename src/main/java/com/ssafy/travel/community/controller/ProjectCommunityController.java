package com.ssafy.travel.community.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.travel.community.dto.CreatePostRequestDto;
import com.ssafy.travel.community.service.CommunityPostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "JWT Auth")
@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
public class ProjectCommunityController {
	
	private final CommunityPostService postService;
	
	@PostMapping("/{projectId}/posts")
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
}
