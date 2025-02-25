package com.hashedin.huSpark.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashedin.huSpark.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Custom UserDetails implementation for Spring Security
 */
public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;

    @JsonIgnore
    private String password;

    private boolean isAdmin;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password, boolean isAdmin,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.authorities = authorities;
    }

    /**
     * Create UserPrincipal from User entity
     * @param user User
     * @return UserPrincipal
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getIsAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getIsAdmin(),
                authorities
        );
    }

    public Long getId(){
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}