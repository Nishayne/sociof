package com.hashedin.huSpark.entity;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a post in the SOCIO platform
 */
@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    private String fileUrl;

    private String fileType;

    private int likes;

    @Column(nullable = false)
    private boolean isShared;

    private Long originalPostId;

    private Long originalUserId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference // Prevent circular serialization
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy load to avoid circular loading
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Comment> comments = new CopyOnWriteArraySet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Like> postLikes = new CopyOnWriteArraySet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Report> reports = new CopyOnWriteArraySet<>();

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}