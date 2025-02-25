package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.Date;

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
    private Long userId;
    private String userEmail;
    private Long groupId;
    private String groupName;
    private Date createdAt;
}

