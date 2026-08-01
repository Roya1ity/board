package com.example.board.reaction;

import com.example.board.Global.Entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction,Long> {

    Optional<CommentReaction> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentIdAndType(Long commentId, ReactionType type);
}
