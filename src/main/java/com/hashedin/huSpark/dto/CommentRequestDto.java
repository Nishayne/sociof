package com.hashedin.huSpark.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    
    @NotBlank(message = "Comment content cannot be blank")
    private String content;
}
