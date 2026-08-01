package com.example.board.Global.Entity;

import com.example.board.post.dto.PostDTO;
import com.example.board.post.dto.PostImageResponse;
import com.example.board.reaction.dto.ReactionResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body",columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="board_id",nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    @BatchSize(size=100)
    private List<PostImage> images = new ArrayList<>();

    @Column(nullable = false)
    private long viewCount;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static PostDTO toDTO(Post post,Long loginUserId,ReactionResponse reaction) {
        boolean owner = loginUserId != null && post.isAuthor(loginUserId);

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setUser(post.getUser().getNick());
        dto.setBoard(post.getBoard().getName());
        dto.setBody(post.getBody());
        dto.setCreateAt(post.getCreatedAt().toString());
        dto.setCanEdit(owner);
        dto.setCanDelete(owner);
        dto.setLike(reaction.getLikeCount());
        dto.setDislike(reaction.getDislikeCount());
        dto.setMyReaction(reaction.getMyReaction());

        List<PostImageResponse> images = post.images.stream().map(PostImage::toDTO).toList();
        dto.setImages(images);
        dto.setViewCount(post.getViewCount());

        return dto;
    }

    public static PostDTO toDTO(Post post,ReactionResponse reaction) {

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setUser(post.getUser().getNick());
        dto.setBoard(post.getBoard().getName());
        dto.setBody(post.getBody());
        dto.setCreateAt(post.getCreatedAt().toString());
        dto.setCanEdit(false);
        dto.setCanDelete(false);
        dto.setLike(reaction.getLikeCount());
        dto.setDislike(reaction.getDislikeCount());
        dto.setMyReaction(reaction.getMyReaction());

        List<PostImageResponse> images = post.images.stream().map(PostImage::toDTO).toList();
        dto.setImages(images);
        dto.setViewCount(post.getViewCount());
        return dto;
    }

    public static PostDTO toDTO(Post post) {

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setUser(post.getUser().getNick());
        dto.setBoard(post.getBoard().getName());
        dto.setBody(post.getBody());
        dto.setCreateAt(post.getCreatedAt().toString());
        dto.setCanEdit(false);
        dto.setCanDelete(false);

        List<PostImageResponse> images = post.images.stream().map(PostImage::toDTO).toList();
        dto.setImages(images);
        dto.setViewCount(post.getViewCount());
        return dto;
    }

    public boolean isAuthor(Long userId) {
        return user.getId().equals(userId);
    }

    public void addImage(PostImage img) {
        images.add(img);
        img.assignPost(this);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
