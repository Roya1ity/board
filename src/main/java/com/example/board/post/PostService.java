package com.example.board.post;

import com.example.board.Global.Entity.Board;
import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.User;
import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.ForbidenException;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.UserRepository;
import com.example.board.board.BoardRepository;
import com.example.board.post.dto.PostRequest;
import com.example.board.post.dto.PostDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public User requiredLogin(Long loginUserId) {
        if (loginUserId == null) {
            throw new UnauthorizedException(ErrorCode.LOGIN_REQUIRED);
        }

        User user = userRepository.findById(loginUserId).orElseThrow(()->new NotFoundUserException(ErrorCode.USER_NOT_FOUND));

        return user;
    }

    @Transactional
    public PostDTO create(Long loginUserId, Long boardId, PostRequest req) {

        System.out.println("Board ID: " + boardId);
        System.out.println("User ID: " + loginUserId);

        Board board = boardRepository.findById(boardId).orElseThrow(()->new NotFoundUserException(ErrorCode.BOARD_NOT_FOUND));

        User user = requiredLogin(loginUserId);

        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setBody(req.getBody());
        post.setBoard(board);
        post.setUser(user);

        Post res = postRepository.save(post);

        return Post.toDTO(res);
    }

    @Transactional(readOnly = true)
    public List<PostDTO> list() {

        return postRepository.findAll().stream().map(Post::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PostDTO read(Long id) {
        return Post.toDTO(postRepository.findById(id).orElseThrow(()->new NotFoundUserException(ErrorCode.POST_NOT_FOUND)));
    }

    @Transactional
    public PostDTO update(Long loginUserId,Long id,@Valid PostRequest req) {
        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundUserException(ErrorCode.POST_NOT_FOUND));

        User user = post.getUser();

        if (!Objects.equals(user.getId(),loginUserId)) {
            throw new ForbidenException(ErrorCode.POST_ACCESS_DENIED);
        }

        post.setTitle(req.getTitle());
        post.setBody(req.getBody());

        Post res = postRepository.save(post);

        return Post.toDTO(res);
    }

    @Transactional
    public IngestResult delete(Long loginUserId,Long id) {
        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundUserException(ErrorCode.POST_NOT_FOUND));
        User user = post.getUser();

        if (!Objects.equals(user.getId(),loginUserId)) {
            throw new ForbidenException(ErrorCode.POST_ACCESS_DENIED);
        }

        postRepository.deleteById(id);

        return new IngestResult("OK","삭제완료");
    }
}
