package com.hashedin.huSpark.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Date;

/**
 * DTO for user update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @Email
    private String email;
    private Boolean isProfilePrivate;
    private Date dateOfBirth;
}

