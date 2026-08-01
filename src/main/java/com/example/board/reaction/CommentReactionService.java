package com.example.board.reaction;

import com.example.board.Global.Entity.Comment;
import com.example.board.Global.Entity.CommentReaction;
import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.UserRepository;
import com.example.board.comment.CommentRepository;
import com.example.board.reaction.dto.ReactionResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentReactionService {

    private final CommentReactionRepository commentReactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReactionResponse reactToComment(Long commentId, Long userId, @NotNull ReactionType type) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        commentReactionRepository.findByCommentIdAndUserId(commentId, userId)
                .ifPresentOrElse(
                        reaction -> toggleExistingReaction(reaction, type),
                        () -> createReaction(comment, user, type)
                );

        return buildCommentReactionResponse(commentId, userId);
    }

    @Transactional(readOnly = true)
    public ReactionResponse buildCommentReactionResponse(Long commentId, Long userId) {
        ReactionResponse response = new ReactionResponse();
        response.setLikeCount(commentReactionRepository.countByCommentIdAndType(commentId, ReactionType.LIKE));
        response.setDislikeCount(commentReactionRepository.countByCommentIdAndType(commentId, ReactionType.DISLIKE));
        response.setMyReaction(userId == null
                ? null
                : commentReactionRepository.findByCommentIdAndUserId(commentId, userId)
                        .map(CommentReaction::getType)
                        .orElse(null));

        return response;
    }

    private void toggleExistingReaction(CommentReaction reaction, ReactionType requestedType) {
        if (reaction.getType() == requestedType) {
            commentReactionRepository.delete(reaction);
            commentReactionRepository.flush();
            return;
        }

        reaction.changeType(requestedType);
        commentReactionRepository.saveAndFlush(reaction);
    }

    private void createReaction(Comment comment, User user, ReactionType type) {
        CommentReaction reaction = CommentReaction.builder()
                .comment(comment)
                .user(user)
                .type(type)
                .build();
        commentReactionRepository.saveAndFlush(reaction);
    }
}
