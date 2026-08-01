package com.example.board.reaction;

import com.example.board.Global.Entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction,Long> {

    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostIdAndType(Long postId, ReactionType type);
}
