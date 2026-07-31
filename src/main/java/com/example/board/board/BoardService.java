package com.example.board.board;


import com.example.board.Global.Entity.Board;
import com.example.board.Global.Entity.Post;
import com.example.board.Global.Entity.User;
import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.ForbidenException;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.auth.UserRepository;
import com.example.board.board.dto.BoardRequest;
import com.example.board.board.dto.BoardResponse;
import com.example.board.comment.CommentRepository;
import com.example.board.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void validateUser(Long loginUserId) {
        User user = userRepository.findById(loginUserId).orElseThrow(
                () -> new NotFoundUserException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.ADMIN) {
            throw new ForbidenException(ErrorCode.BOARD_ACCESS_DENIED);
        }
    }

    @Transactional
    public BoardResponse create(BoardRequest req) {

        //validateUser(loginUserId);

        if (boardRepository.existsByName(req.getName())) {
            throw new ForbidenException(ErrorCode.DUPLICATE_BOARD_NAME);
        }
        Board board = new Board();
        board.setName(req.getName());
        board.setDescription(req.getDescription());

        Board savedBoard = boardRepository.save(board);
        BoardResponse res = new BoardResponse();
        res.setId(savedBoard.getId());
        res.setName((savedBoard.getName()));
        res.setDescription(savedBoard.getDescription());
        res.setCreateAt(savedBoard.getCreatedAt().toString());

        return res;
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> list() {

        return boardRepository.findAll().stream().map(Board::toDTO).toList();
    }

    @Transactional
    public BoardResponse update(Long boardId, BoardRequest req) {
        //validateUser(loginUserId);

        Board board = boardRepository.findById(boardId).orElseThrow(()->new NotFoundUserException(ErrorCode.BOARD_NOT_FOUND));
        board.setName(req.getName());
        board.setDescription(req.getDescription());

        Board savedBoard = boardRepository.save(board);
        BoardResponse res = new BoardResponse();
        res.setId(savedBoard.getId());
        res.setName((savedBoard.getName()));
        res.setDescription(savedBoard.getDescription());
        res.setCreateAt(savedBoard.getCreatedAt().toString());

        return res;
    }

    @Transactional
    public IngestResult delete(Long boardId) {
        //validateUser(loginUserId);

        if (!boardRepository.existsById(boardId)) {
            throw new NotFoundUserException(ErrorCode.BOARD_NOT_FOUND);
        }

        for (Post post: postRepository.findByBoardId(boardId)) {
            commentRepository.deleteByPostId(post.getId());
        }

        postRepository.deleteByBoardId(boardId);

        boardRepository.deleteById(boardId);


        return new IngestResult("OK","삭제완료");
    }
}
