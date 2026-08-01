package com.example.board.reaction;

import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.PostReaction;
import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.UserRepository;
import com.example.board.post.PostRepository;
import com.example.board.reaction.dto.ReactionResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostReactionService {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReactionResponse postReacttion(Long postId, Long userId, @NotNull ReactionType type) {

        Post post = postRepository.findById(postId).orElseThrow(()->new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));
        postReactionRepository.findByPostIdAndUserId(postId, userId)
                .ifPresentOrElse(
                        reaction -> toggleExistingReaction(reaction, type),
                        () -> createReaction(post, user, type)
                );

        return buildPostReactionResponse(postId, userId);
    }

    @Transactional(readOnly = true)
    public ReactionResponse buildPostReactionResponse(Long postId, Long userId) {
        ReactionResponse res = new ReactionResponse();
        res.setLikeCount(postReactionRepository.countByPostIdAndType(postId,ReactionType.LIKE));
        res.setDislikeCount(postReactionRepository.countByPostIdAndType(postId,ReactionType.DISLIKE));
        res.setMyReaction(userId == null
                ? null
                : postReactionRepository.findByPostIdAndUserId(postId,userId)
                        .map(PostReaction::getType)
                        .orElse(null));

        return res;
    }

    private void toggleExistingReaction(PostReaction reaction, ReactionType requestedType) {
        if (reaction.getType() == requestedType) {
            postReactionRepository.delete(reaction);
            postReactionRepository.flush();
            return;
        }

        reaction.changeType(requestedType);
        postReactionRepository.saveAndFlush(reaction);
    }

    private void createReaction(Post post, User user, ReactionType type) {
        PostReaction reaction = PostReaction.builder()
                .post(post)
                .user(user)
                .type(type)
                .build();
        postReactionRepository.saveAndFlush(reaction);
    }
}
