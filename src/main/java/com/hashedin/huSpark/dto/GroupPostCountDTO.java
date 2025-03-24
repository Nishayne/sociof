package com.hashedin.huSpark.dto;

import com.hashedin.huSpark.entity.Group;

public class GroupPostCountDTO {
    private Group group;
    private Long postCount;

    public GroupPostCountDTO(Group group, Long postCount) {
        this.group = group;
        this.postCount = postCount;
    }

    public Group getGroup() {
        return group;
    }

    public Long getPostCount() {
        return postCount;
    }
}