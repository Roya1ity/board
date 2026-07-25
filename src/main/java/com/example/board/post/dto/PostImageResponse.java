package com.example.board.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostImageResponse {

    private Long id;
    private String url;
    private String originalName;
    private int sortOrder;

}
