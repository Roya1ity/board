package com.example.board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String author,
        String content,
        boolean deleted,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {
    public static final String DELETED_CONTENT = "삭제된 게시글입니다";
}
