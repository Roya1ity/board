package com.example.board.Global.Entity;

import com.example.board.post.dto.PostImageResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name="post_image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private String storedName;

    private String originalName;
    private String contentType;
    private long size;

    @Column(nullable = false)
    private int sortOrder;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private static final String URL_PREFIX = "/images/";

    public void assignPost(Post post) {
        this.post = post;
    }

    public static PostImageResponse toDTO(PostImage img) {
        PostImageResponse dto = new PostImageResponse();
        dto.setId(img.getId());
        dto.setUrl(URL_PREFIX + img.getStoredName());
        dto.setOriginalName(img.getOriginalName());
        dto.setSortOrder(img.getSortOrder());

        return dto;
    }
}
