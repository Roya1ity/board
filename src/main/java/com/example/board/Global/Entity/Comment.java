package com.example.board.Global.Entity;

import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.board.comment.dto.CommentResponse.DELETED_CONTENT;

@Entity
@Table(name="commnent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id",nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("createdAt asc")
    @BatchSize(size = 100)
    private List<Comment> children = new ArrayList<>();

    @Column(name = "body",columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void addReply(Comment reply) {
        children.add(reply);
        reply.parent = this;
    }

    public void update(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.deleted = true;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isReply() {
        return parent != null;
    }

    public boolean isAuthor(Long userId) {
        return user.getId().equals(userId);
    }

    public static CommentResponse toResponse(Comment comment) {
        List<CommentResponse> children = comment.isRoot()
                ? comment.getChildren().stream().map((Comment::toResponse)).toList()
                : List.of();

        return new CommentResponse(
                comment.getId(),
                comment.getUser().getNick(),
                comment.isDeleted() ? DELETED_CONTENT : comment.getContent(),
                comment.isDeleted(),
                comment.getCreateAt(),
                children
        );
    }

}
