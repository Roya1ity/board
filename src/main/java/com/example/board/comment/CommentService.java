package com.example.board.comment;

import com.example.board.Global.Entity.Comment;
import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.LoginUserId;
import com.example.board.auth.UserRepository;
import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.post.PostRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;

    public CommentResponse create(Long loginUserId, Long postId, CommentCreateRequest req) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(loginUserId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Comment parent = commentRepository.findById(req.getParentId()).orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(req.getContent());
        comment.setUser(user);
        comment.setParent(parent);
        comment.setDeleted(false);

        Comment savedComment = commentRepository.save(comment);

        return Comment.toResponse(savedComment);
    }

    public Page<CommentResponse> getComments(Long postId, Pageable page) {

        Page<CommentResponse> res = commentRepository.findByPostId(postId,page).map(Comment::toResponse);

        return res;
    }

    public CommentResponse update(Long commentId, @Valid CommentCreateRequest req) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        comment.update(req.getContent());
        Comment savedComment = commentRepository.save(comment);

        return Comment.toResponse(savedComment);
    }

    public void delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isDeleted()) {
            comment.setDeleted(true);
        }
    }
}
