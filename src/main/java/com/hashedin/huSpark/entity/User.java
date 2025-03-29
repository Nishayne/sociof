package com.hashedin.huSpark.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user in the SOCIO platform
 */

 /*@Data includes @Getter and @Setter, but also generates equals() and hashCode(), 
 which can cause issues with entities using @OneToMany and @ManyToMany */
@Entity
@Getter
@Setter
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User  implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + role.name());
        //return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Column(nullable = false)
    private Boolean isAdmin;

    @Column(nullable = false)
    private Boolean isProfilePrivate;

    private Date dateOfBirth;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    private Date passwordUpdatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                                    // and Manage Cyclic Dependencies
                                                    // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                            // Post →(references) Group
    @Builder.Default
    private Set<Post> posts = new CopyOnWriteArraySet<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Follow> following = new CopyOnWriteArraySet<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Follow> followers = new CopyOnWriteArraySet<>();

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Group> createdGroups = new CopyOnWriteArraySet<>();

    @ManyToMany(mappedBy = "members", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // change LAZY vs Eager loading for members, Prevent ConcurrentModificationException 
                                        // and Manage Cyclic Dependencies
                                        // User →(is part of) Group →(has) Group Members → User →(creates) 
                                                                                                // Post →(references) Group
    @Builder.Default
    private Set<Group> memberGroups = new CopyOnWriteArraySet<>();

    @Override
    public String getUsername() {
        return email;
    }
}
