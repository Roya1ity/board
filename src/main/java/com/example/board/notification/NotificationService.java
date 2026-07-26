package com.example.board.notification;

import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.UserRepository;
import com.example.board.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(Long recipientId, Long actorId, NotificationType type, Long postId, Long commentId) {

        User recipient = userRepository.findById(recipientId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));
        User actor = userRepository.findById(actorId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .notificationType(type)
                .postId(postId)
                .commentId(commentId)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long id, Pageable page) {
        return notificationRepository.findByRecipientId(id,page).map(Notification::toResponse);
    }

    @Transactional
    public void read(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if ( !notification.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_VIEW_NOTIFICATION);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public long unreadCount(Long id) {
        return notificationRepository.countByRecipientIdAndReadIsFalse(id);
    }
}
