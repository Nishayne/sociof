package com.hashedin.huSpark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for post creation/update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank
    private String content;

    private String fileUrl;
    private String fileType;
    @NotNull
    private Long groupId;
}