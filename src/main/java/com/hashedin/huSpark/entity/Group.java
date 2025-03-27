package com.hashedin.huSpark.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a group in the SOCIO platform
 */
@Entity
@Table(name = "groups_")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    private static final Logger log = LoggerFactory.getLogger(Group.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isPrivate;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY) // testing change back to LAZY, Eager loading for members, Prevent ConcurrentModificationException and Manage Cyclic Dependencies
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new /*CopyOnWriteArraySet*/HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // testing change back to LAZY, Eager loading for posts,  Prevent ConcurrentModificationException and Manage Cyclic Dependencies
    @Builder.Default
    private Set<Post> posts = new /*CopyOnWriteArraySet*/HashSet<>();

    public Set<User> getMembers() {
        log.info("Accessing members for group: {}", this.id);
        return members;
    }

    public Set<Post> getPosts() {
        log.info("Accessing posts for group: {}", this.id);
        return posts;
    }

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}