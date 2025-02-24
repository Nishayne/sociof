package com.hashedin.huSpark.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name="creator_id",nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
            name="group_members",
            joinColumns=@JoinColumn(name="group_id"),
            inverseJoinColumns = @JoinColumn(name="user_id")
    )
    private Set<User> members=new HashSet<>();

    @Column(nullable=false)
    private boolean isPrivate;
}
