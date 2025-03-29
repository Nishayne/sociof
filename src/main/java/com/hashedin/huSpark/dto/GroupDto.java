package com.hashedin.huSpark.dto;

import java.util.Date;
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for group representation
 */

/*@Data includes @Getter and @Setter, but also generates equals() and hashCode(), 
 which can cause issues with entities using @OneToMany and @ManyToMany */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupDto {
    private Long id;
    private String name;
    private Boolean isPrivate;
    private Long creatorId;
    private String creatorEmail;
    private int memberCount;
    private int postCount;
    private Date createdAt;
        public GroupDto(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.isPrivate = group.getIsPrivate();

        User creator = group.getCreator();
        this.creatorId = creator.getId();
        this.creatorEmail = creator.getEmail();
        
        this.memberCount = group.getMembers().size();
        this.postCount = group.getPosts().size();

        this.createdAt = group.getCreatedAt();
    }
}
