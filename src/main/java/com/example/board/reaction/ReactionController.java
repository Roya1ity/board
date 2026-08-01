package com.example.board.reaction;

import com.example.board.auth.CustomUserDetails;
import com.example.board.reaction.dto.ReactionRequest;
import com.example.board.reaction.dto.ReactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reation")
@RequiredArgsConstructor
public class ReactionController {

    private final PostReactionService postReactionService;
    private final CommentReactionService commentReactionService;

    @PostMapping("/post/{postId}")
    public ReactionResponse reactToPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody ReactionRequest req
            ) {
        return postReactionService.postReacttion(postId,userDetails.getId(),req.type());
    }

    @PostMapping("/comment/{commentId}")
    public ReactionResponse reactToComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody ReactionRequest req
    ) {
        return commentReactionService.reactToComment(commentId, userDetails.getId(), req.type());
    }
}
