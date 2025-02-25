package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Date;

/**
 * DTO for user representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private Boolean isAdmin;
    private Boolean isProfilePrivate;
    private Date dateOfBirth;
    private Date createdAt;
    private long followerCount;
    private long followingCount;
}