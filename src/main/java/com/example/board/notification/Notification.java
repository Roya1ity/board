package com.example.board.notification;

import com.example.board.Global.Entity.User;
import com.example.board.notification.dto.NotificationResponse;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id",nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id",nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "post_id",nullable = false)
    private Long postId;

    @Column(name = "comment_id")
    private Long commentId;

    @Builder.Default
    @Column(name = "is_read",nullable = false)
    private boolean read = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void markAsRead() {
        this.read = true;
    }

    public boolean isOwnedBy(Long userId) {
        return recipient.getId().equals(userId);
    }

    public static NotificationResponse toResponse(Notification notification) {
        NotificationResponse res = new NotificationResponse();
        res.setId(notification.getId());
        res.setType(notification.getNotificationType());
        res.setActor(notification.getActor().getNick());
        res.setRead(notification.isRead());
        res.setPostId(notification.getPostId());
        res.setCommentId(notification.getCommentId());
        res.setCreatedAt(notification.getCreatedAt());
        String message = notification.getNotificationType() == NotificationType.REPLY_ON_COMMENT
                ? notification.actor.getNick() + "님이 댓글에 댓글을 달았습니다."
                : notification.actor.getNick() + "님이 게시글에 댓글을 달았습니다.";
        res.setMessage(message);

        return res;
    }
}
