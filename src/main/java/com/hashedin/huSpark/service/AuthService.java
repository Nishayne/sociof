package com.hashedin.huSpark.service;

import com.hashedin.huSpark.model.Role;
import com.hashedin.huSpark.model.User;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository =userRepository;
        this.passwordEncoder =passwordEncoder;
    }

    public User registerUser(String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .isPublic(true)
                .build();
        return userRepository.save(user);
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
