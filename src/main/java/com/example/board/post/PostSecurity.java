package com.example.board.post;

import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("postSecurity")
@RequiredArgsConstructor
public class PostSecurity {

    private final PostRepository postRepository;

    @Transactional
    public boolean isAuthor(Long postId, CustomUserDetails user) {

        log.debug("PostSecurity.isAuthor user.email: {}",user.getUsername());

        Long userId = postRepository.findByUserIdById(postId)
                .orElseThrow(()->new NotFoundUserException(ErrorCode.POST_NOT_FOUND));

        return userId.equals(user.getId());
    }

}
