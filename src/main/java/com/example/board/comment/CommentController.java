package com.example.board.comment;

import com.example.board.Global.Entity.Comment;
import com.example.board.auth.CustomUserDetails;
import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.post.dto.PostDTO;
import com.example.board.post.dto.PostRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/new")
    public CommentResponse create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest req
    ) {

        return commentService.create(userDetails.getId(),postId,req);
    }

    @GetMapping("/{postId}/list")
    public Page<CommentResponse> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt",direction = Sort.Direction.DESC) Pageable page
    ) {

        Long loginUserId = userDetails == null ? null : userDetails.getId();
        return commentService.getComments(loginUserId,postId,page);
    }

    @PreAuthorize("@commentSecurity.isAuthor(#commentId, authentication.principal)")
    @PutMapping("/{commentId}/update")
    public CommentResponse update(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentCreateRequest req
    ) {


        return commentService.update(commentId,req);
    }

    @PreAuthorize("@commentSecurity.isAuthor(#commentId, authentication.principal)")
    @DeleteMapping("/{commentId}/delete")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {

        commentService.delete(commentId);
        return ResponseEntity.ok().build();
    }
}
