package com.example.board.comment;

import com.example.board.Global.Entity.Comment;
import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.UserRepository;
import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.notification.CommentCreateEvent;
import com.example.board.post.PostRepository;
import com.example.board.reaction.CommentReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentReactionService commentReactionService;

    @Transactional
    public CommentResponse create(Long loginUserId, Long postId, CommentCreateRequest req) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(loginUserId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(req.getContent());
        comment.setUser(user);
        if (req.getParentId() != null) {
            Comment parent = commentRepository.findById(req.getParentId()).orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
            comment.setParent(parent);
        }
        comment.setDeleted(false);

        Comment savedComment = commentRepository.save(comment);

        eventPublisher.publishEvent(
                new CommentCreateEvent(
                savedComment.getId(),
                postId,
                req.getParentId(),
                loginUserId
        ));


        return Comment.toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long loginUserId, Long postId, Pageable page) {

        Page<CommentResponse> res = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId,page)
                .map(comment -> Comment.toResponse(
                        comment,
                        loginUserId,
                        commentId -> commentReactionService.buildCommentReactionResponse(commentId, loginUserId)
                ));

        return res;
    }

    @Transactional
    public CommentResponse update(Long commentId, @Valid CommentCreateRequest req) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        comment.update(req.getContent());
        Comment savedComment = commentRepository.save(comment);

        if (comment.isDeleted()) {
            throw new BusinessException(ErrorCode.CANNOT_COMMENT);
        }

        return Comment.toResponse(savedComment);
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
