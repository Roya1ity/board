package com.example.board.comment;

import com.example.board.Global.Entity.Comment;
import com.example.board.Global.Entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    Page<Comment> findByPostId(Long postId, Pageable page);

    @Query("select p.user.id from Comment p where p.id = :id")
    Optional<Long> findByUserIdById(Long id);
}
