package com.example.board.post;

import com.example.board.Global.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findByBoardId(Long boardId);

    @Query("select p.user.id from Post p where p.id = :id")
    Optional<Long> findByUserIdById(Long id);
}
