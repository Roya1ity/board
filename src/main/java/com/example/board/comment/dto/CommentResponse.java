package com.example.board.comment.dto;

import com.example.board.reaction.ReactionType;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String author,
        String content,
        boolean deleted,
        LocalDateTime createdAt,
        boolean canEdit,
        boolean canDelete,
        long likeCount,
        long dislikeCount,
        ReactionType myReaction,
        List<CommentResponse> children
) {
    public static final String DELETED_CONTENT = "삭제된 댓글입니다.";
}
