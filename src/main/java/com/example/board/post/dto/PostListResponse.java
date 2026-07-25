package com.example.board.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

    private Long id;
    private String title;
    private String author;
    private long viewCount;
    private String thumbnailUrl;
    private LocalDateTime createdAt;

}
