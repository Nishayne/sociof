package com.hashedin.huSpark.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for post representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    private String content;
    private String fileUrl;
    private String fileType;
    private int likes;
    private int comments;
    private boolean isShared;
    private Long originalPostId;
    private Long originalUserId;
    private Date createdAt;
}

