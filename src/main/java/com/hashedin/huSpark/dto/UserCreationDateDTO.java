package com.hashedin.huSpark.dto;

import java.util.Date;

public class UserCreationDateDTO {
    private Date date;
    private Long count;

    public UserCreationDateDTO(Date date, Long count) {
        this.date = date;
        this.count = count;
    }

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