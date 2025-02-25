package com.hashedin.huSpark.dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Request object for sharing a post
 */
@Getter
@Setter
public class ShareRequest {
    private String customMessage; // Optional message to include with the share
}
