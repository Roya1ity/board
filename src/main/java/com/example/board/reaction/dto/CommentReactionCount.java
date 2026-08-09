package com.example.board.reaction.dto;

import com.example.board.reaction.ReactionType;

public record CommentReactionCount(Long commentId, ReactionType type, long count) {
}
