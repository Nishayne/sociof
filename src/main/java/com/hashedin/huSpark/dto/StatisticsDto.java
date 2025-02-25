package com.hashedin.huSpark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for statistics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private Object data;
    private String type;
    private String label;
}
