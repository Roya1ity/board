package com.example.board.user;

import com.example.board.Global.Entity.Board;
import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.User;
import com.example.board.Global.Entity.UserProfile;
import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.ForbidenException;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.UserRepository;
import com.example.board.board.BoardRepository;
import com.example.board.post.PostRepository;
import com.example.board.post.dto.PostDTO;
import com.example.board.post.dto.PostRequest;
import com.example.board.user.dto.UserProfileDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileDTO me(Long loginUserId) {
        UserProfile user = userProfileRepository.findById(loginUserId).orElseThrow(()->new NotFoundUserException(ErrorCode.USER_NOT_FOUND));

        UserProfileDTO res = UserProfile.toDTO(user);

        return res;
    }
}
