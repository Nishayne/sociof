package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO for group representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long id;
    private String name;
    private Boolean isPrivate;
    private Long creatorId;
    private String creatorEmail;
    private int memberCount;
    private int postCount;
    private Date createdAt;
}
