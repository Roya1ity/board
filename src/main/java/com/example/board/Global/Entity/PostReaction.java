package com.example.board.Global.Entity;

import com.example.board.reaction.ReactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_reaction",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_reaction",columnNames = {"post_id","user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id",nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private ReactionType type;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void changeType(ReactionType type) {
        this.type = type;
    }

}
