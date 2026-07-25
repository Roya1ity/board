package com.example.board.comment;

import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.auth.CustomUserDetails;
import com.example.board.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component("commentSecurity")
@RequiredArgsConstructor
public class CommentSecurity {

        private final CommentRepository commentRepository;

        @Transactional
        public boolean isAuthor(Long commentId, CustomUserDetails user) {

            log.debug("CommentSecurity.isAuthor user.email: {}",user.getUsername());

            Long userId = commentRepository.findByUserIdById(commentId)
                    .orElseThrow(()->new NotFoundUserException(ErrorCode.COMMENT_NOT_FOUND));

            return userId.equals(user.getId());
        }

}
