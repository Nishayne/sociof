package com.hashedin.huSpark.dto;

public class PostFileTypeCountDTO{
    private String fileType;
    private Long count;

    public PostFileTypeCountDTO(String fileType, Long count) {
        this.fileType = fileType;
        this.count = count;
    }
    //getters and setters.
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}