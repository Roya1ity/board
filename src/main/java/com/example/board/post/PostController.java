package com.example.board.post;

import com.example.board.Global.IngestResult;
import com.example.board.auth.AuthController;
import com.example.board.auth.LoginUserId;
import com.example.board.post.dto.PostRequest;
import com.example.board.post.dto.PostDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    @PostMapping("/{boardId}/new")
    public PostDTO create(
            @LoginUserId Long loginUserId,
            @PathVariable Long boardId,
            @Valid @RequestBody PostRequest req
    ) {

        return postService.create(loginUserId,boardId,req);
    }

    @GetMapping("/all")
    public List<PostDTO> list() {

        return postService.list();
    }

    @GetMapping("/{id}")
    public PostDTO read(@PathVariable long id) {
        return postService.read(id);
    }

    @PutMapping("/{id}/update")
    public PostDTO update(
            @LoginUserId Long loginUserId,
            @PathVariable Long id,
            @Valid @RequestBody PostRequest req
    ) {
        return postService.update(loginUserId,id,req);
    }

    @DeleteMapping("/{id}/delete")
    public IngestResult delete(
            @LoginUserId Long loginUserId,
            @PathVariable Long id
    ) {
        return postService.delete(loginUserId,id);
    }
}
