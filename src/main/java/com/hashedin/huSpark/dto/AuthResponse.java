package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for authentication response
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
}