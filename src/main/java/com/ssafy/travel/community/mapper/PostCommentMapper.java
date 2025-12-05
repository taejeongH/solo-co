package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.CommentDto;

@Mapper
public interface PostCommentMapper {

    List<CommentDto> findCommentsByPostId(@Param("postId") Long postId);
}
