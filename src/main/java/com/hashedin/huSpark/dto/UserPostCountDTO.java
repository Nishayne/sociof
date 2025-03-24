package com.hashedin.huSpark.dto;

public class UserPostCountDTO {
    private Long userId;
    private Long count;

    public UserPostCountDTO(Long userId, Long count) {
        this.userId = userId;
        this.count = count;
    }
    //getters and setters.
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
