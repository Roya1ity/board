package com.example.board.post;

import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.AuthController;
import com.example.board.auth.CustomUserDetails;
import com.example.board.auth.LoginUserId;
import com.example.board.post.dto.PostRequest;
import com.example.board.post.dto.PostDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private static final int MAX_IMAGE_COUNT = 3;
    private final PostService postService;

    @PostMapping("/{boardId}/new")
    public PostDTO create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long boardId,
            @Valid @RequestPart("post") PostRequest post,
            @RequestPart(value = "images",required = false) List<MultipartFile> images
    ) {
        validateImageCount(images);
        return postService.create(userDetails.getId(),boardId,post,images);
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images != null && images.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
    }

    @GetMapping("/all")
    public List<PostDTO> list() {

        return postService.list();
    }

    @GetMapping("/{boardId}/all")
    public Page<PostDTO> currentBoardList(@PathVariable Long boardId,
                                          @PageableDefault(size = 10,sort = "createdAt",direction = Sort.Direction.DESC) Pageable page) {

        return postService.currentBoardList(boardId,page);
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
