package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.response.CommentResponseDto;

@Mapper
public interface ProjectPostCommentMapper {
    List<CommentResponseDto> findCommentsByPostId(@Param("postId") Long postId);
    int deleteCommentsByPostId(@Param("postId") Long postId);
}
