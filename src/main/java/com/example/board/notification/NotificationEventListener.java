package com.example.board.notification;

import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.comment.CommentRepository;
import com.example.board.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCommentCreated(CommentCreateEvent event) {
        log.debug("댓글 생성 이벤트 발생됨");

        NotificationType type;
        Long recipientId = null;

        if (event.parentCommentId() == null) {
            recipientId = postRepository.findByUserIdById(event.postId()).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
            type = NotificationType.COMMENT_ON_POST;
        }
        else {
            recipientId = commentRepository.findByUserIdById(event.parentCommentId()).orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));
            type = NotificationType.REPLY_ON_COMMENT;
        }

        if (recipientId == null || recipientId.equals(event.actorId())) {
            log.error("이벤트를 받을 대상이 없거나, 이벤트 리스너와 발생자가 동일함");
            return;
        }

        try {
            notificationService.create(recipientId,event.actorId(),type,event.postId(), event.commentId());
        }
        catch (RuntimeException e) {
            log.error("알림 저장중 에러 발생 : {}",e.getMessage());
        }
    }
}
