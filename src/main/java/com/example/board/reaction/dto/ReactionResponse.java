package com.example.board.reaction.dto;

import com.example.board.reaction.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {

    private long likeCount;
    private long dislikeCount;
    private ReactionType myReaction;

}
