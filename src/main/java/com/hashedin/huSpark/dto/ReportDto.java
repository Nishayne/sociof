package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO for report representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private String justification;
    private String status;
    private Long postId;
    private String postContent;
    private Long reporterId;
    private String reporterEmail;
    private Long moderatorId;
    private String moderatorEmail;
    private Date moderatedAt;
    private Date createdAt;
}

