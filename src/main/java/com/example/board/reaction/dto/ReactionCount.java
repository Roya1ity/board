package com.example.board.reaction.dto;

import com.example.board.reaction.ReactionType;

public record ReactionCount(ReactionType type, long count) {
}
