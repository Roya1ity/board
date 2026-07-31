package com.example.board.post;

import com.example.board.Global.Entity.Board;
import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.PostImage;
import com.example.board.Global.Entity.User;
import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.ForbidenException;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.UserRepository;
import com.example.board.board.BoardRepository;
import com.example.board.comment.CommentRepository;
import com.example.board.post.dto.PostRequest;
import com.example.board.post.dto.PostDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public PostDTO create(Long loginUserId, Long boardId, PostRequest req, List<MultipartFile> images) {

        System.out.println("Board ID: " + boardId);
        System.out.println("User ID: " + loginUserId);

        Board board = boardRepository.findById(boardId).orElseThrow(()->new NotFoundUserException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findById(loginUserId).orElseThrow(()->new NotFoundUserException(ErrorCode.USER_NOT_FOUND));

        List<String> storedNames = new ArrayList<>();
        try {
            Post post = new Post();
            post.setTitle(req.getTitle());
            post.setBody(req.getBody());
            post.setBoard(board);
            post.setUser(user);

            if ( images != null) {
                int order = 0;
                for(MultipartFile file: images) {
                    String storedName = fileStorageService.store(file);
                    storedNames.add(storedName);
                    PostImage img = PostImage.builder()
                                    .storedName(storedName)
                                    .originalName(file.getOriginalFilename())
                                    .contentType(file.getContentType())
                                    .size(file.getSize())
                                    .sortOrder(order++)
                                    .build();
                    post.addImage(img);
                }
            }

            Post res = postRepository.save(post);
            return Post.toDTO(res);
        }
        catch (RuntimeException e) {
            //삭제처리 해야함
            for(String fileName: storedNames) {
                fileStorageService.delete(fileName);
            }
            throw e;
        }


    }

    @Transactional(readOnly = true)
    public List<PostDTO> list() {
        return postRepository.findAll().stream().map(Post::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> currentBoardList(Long boardId, Pageable page) {
        return postRepository.findByBoardId(boardId,page).map(Post::toDTO);
    }

    @Transactional
    public PostDTO read(Long loginUserId, Long id) {
        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundUserException(ErrorCode.POST_NOT_FOUND));

        if (!post.isAuthor(loginUserId)) {
            post.increaseViewCount();
        }

        return Post.toDTO(post,loginUserId);
    }

    @Transactional
    public PostDTO update(Long id,@Valid PostRequest req) {
        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundUserException(ErrorCode.POST_NOT_FOUND));
//        validateAuthor(post,loginUserId);
//        User user = post.getUser();
//
//        if (!Objects.equals(user.getId(),loginUserId)) {
//            throw new ForbidenException(ErrorCode.POST_ACCESS_DENIED);
//        }

        post.setTitle(req.getTitle());
        post.setBody(req.getBody());

        Post res = postRepository.save(post);

        return Post.toDTO(res);
    }

    @Transactional
    public IngestResult delete(Long id) {
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
//        validateAuthor(post,loginUserId);
//        User user = post.getUser();
//
//        if (!Objects.equals(user.getId(),loginUserId)) {
//            throw new ForbidenException(ErrorCode.POST_ACCESS_DENIED);
//        }


        return new IngestResult("OK","삭제 완료");
    }

    private void validateAuthor(Post post, Long userid) {
        if (!post.isAuthor(userid)) {
            throw new ForbidenException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}
