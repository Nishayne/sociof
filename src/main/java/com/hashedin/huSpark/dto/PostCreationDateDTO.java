package com.hashedin.huSpark.dto;

import java.util.Date;

public class PostCreationDateDTO {
    private Date date;
    private Long count;

    public PostCreationDateDTO(Date date, Long count) {
        this.date = date;
        this.count = count;
    }

    // Getters and setters
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}