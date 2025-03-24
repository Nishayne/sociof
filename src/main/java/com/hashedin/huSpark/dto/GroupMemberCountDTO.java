package com.hashedin.huSpark.dto;

import com.hashedin.huSpark.entity.Group;

public class GroupMemberCountDTO {
    private Group group;
    private Long memberCount;

    public GroupMemberCountDTO(Group group, Long memberCount) {
        this.group = group;
        this.memberCount = memberCount;
    }
    // getters and setters.
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }
}