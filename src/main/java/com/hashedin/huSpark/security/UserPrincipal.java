package com.hashedin.huSpark.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashedin.huSpark.entity.User;

/**
 * Custom UserDetails implementation for Spring Security
 */
public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;

    @JsonIgnore
    private String password;

    private boolean isAdmin;

    // private Collection<? extends GrantedAuthority> authorities;
    private List<GrantedAuthority> authorities; // Change to List<GrantedAuthority>


    private static final Logger log = LoggerFactory.getLogger(UserPrincipal.class);

    public UserPrincipal(Long id, String email, String password, boolean isAdmin,
                         List<GrantedAuthority> authorities) {
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

        List<GrantedAuthority> authorities = new  CopyOnWriteArrayList<>(); // Use CopyOnWriteArrayList to prevent ConcurrentModificationException

        if (user.getIsAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        /*
         * Spring Security checks roles with the default "ROLE_" prefix. 
         * If your UserDetails implementation stores roles as "ADMIN", then:
         * Fix: Change hasRole('ADMIN') → hasAuthority('ROLE_ADMIN')
         */
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        log.info("UserPrincipal created for user: {}", user.getEmail());
        log.info("Authorities: {}", authorities);

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