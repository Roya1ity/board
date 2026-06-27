package com.example.board.board;


import com.example.board.Global.IngestResult;
import com.example.board.auth.AuthController;
import com.example.board.auth.LoginUserId;
import com.example.board.board.dto.BoardRequest;
import com.example.board.board.dto.BoardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping("/new")
    public BoardResponse create(
            @Valid @RequestBody BoardRequest req
    ) {
        return boardService.create(req);
    }

    @GetMapping("/all")
    public List<BoardResponse> list() {

        return boardService.list();
    }

    @PutMapping("/{id}/update")
    public BoardResponse update(
            @PathVariable Long id,
            @RequestBody BoardRequest req) {

        return boardService.update(id,req);
    }

    @DeleteMapping("/{id}/delete")
    public IngestResult delete(
            @PathVariable Long id) {

        return boardService.delete(id);
    }
}
