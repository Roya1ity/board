package com.example.board.comment;

import com.example.board.Global.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    Page<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(Long postId, Pageable page);

    @Query("select p.user.id from Comment p where p.id = :id")
    Optional<Long> findByUserIdById(Long id);

    void deleteByPostId(Long id);
}
