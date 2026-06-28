package com.example.board.post;

import com.example.board.Global.IngestResult;
import com.example.board.auth.AuthController;
import com.example.board.auth.CustomUserDetails;
import com.example.board.auth.LoginUserId;
import com.example.board.post.dto.PostRequest;
import com.example.board.post.dto.PostDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    @PostMapping("/{boardId}/new")
    public PostDTO create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId,
            @Valid @RequestBody PostRequest req
    ) {

        return postService.create(userDetails.getId(),boardId,req);
    }

    @GetMapping("/all")
    public List<PostDTO> list() {

        return postService.list();
    }

    @GetMapping("/{boardId}/all")
    public List<PostDTO> currentBoardList(@PathVariable Long boardId) {

        return postService.currentBoardList(boardId);
    }

    @GetMapping("/{id}")
    public PostDTO read(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable long id) {
        return postService.read(userDetails.getId(),id);
    }

    @PreAuthorize("@postSecurity.isAuthor(#id, authentication.principal)")
    @PutMapping("/{id}/update")
    public PostDTO update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest req
    ) {
        return postService.update(id,req);
    }

    @PreAuthorize("@postSecurity.isAuthor(#id, authentication.principal)")
    @DeleteMapping("/{id}/delete")
    public IngestResult delete(@PathVariable Long id) {

        return postService.delete(id);
    }
}
