package com.example.board.post;

import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.PostImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    @EntityGraph(attributePaths = {"board","user"})
    Page<Post> findByBoardId(Long boardId, Pageable pageable);

    List<Post> findByBoardId(Long boardId);

    @Query("select distinct p from Post p " +
            "join fetch p.board " +
            "join fetch p.user " +
            "left join fetch p.images " +
            "where p.id = :id"
    )
    Optional<Post> findDetailById(@Param("id") Long id);

    @Query("select p.user.id from Post p where p.id = :id")
    Optional<Long> findByUserIdById(Long id);


    void deleteByBoardId(Long boardId);
}
