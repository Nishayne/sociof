package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.Date;

/**
 * DTO for comment representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private String userEmail;
    private Date createdAt;
}
