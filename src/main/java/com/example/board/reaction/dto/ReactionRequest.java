package com.example.board.reaction.dto;

import com.example.board.reaction.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(@NotNull ReactionType type) {
}
