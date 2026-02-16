package com.ssafy.travel.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.community.dto.response.CommentResponseDto;
import com.ssafy.travel.community.entity.ProjectPostComment;

@Mapper
public interface ProjectPostCommentMapper {
        List<CommentResponseDto> findCommentsByPostId(@Param("postId") Long postId);

        int deleteCommentsByPostId(@Param("postId") Long postId);

        int insertComment(
                        @Param("postId") Long postId,
                        @Param("userId") Long userId,
                        @Param("content") String content);

        void updateComment(
                        @Param("commentId") Long commentId,
                        @Param("content") String content);

        void deleteComment(@Param("commentId") Long commentId);

        Long getLastInsertId();

        ProjectPostComment findCommentByCommentId(@Param("commentId") Long commentId);
}
