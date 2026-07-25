package com.example.board.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

    private Long id;
    private String title;
    private String user;
    private String board;
    private String body;
    private long viewCount;
    List<PostImageResponse> images;
    private String createAt;
    private boolean canEdit;
    private boolean canDelete;
}
