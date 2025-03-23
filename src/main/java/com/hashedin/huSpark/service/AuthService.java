package com.hashedin.huSpark.service;

import com.hashedin.huSpark.dto.AuthResponse;
import com.hashedin.huSpark.dto.LoginRequest;
import com.hashedin.huSpark.dto.PasswordResetRequest;
import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UserAlreadyExistsException;
import com.hashedin.huSpark.repository.UserRepository;
import com.hashedin.huSpark.security.JwtTokenProvider;

import ch.qos.logback.classic.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;

/**
 * Service for authentication-related operations
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Register a new user
     * 
     * @param signUpRequest SignUpRequest
     * @return User
     */
    public User registerUser(SignUpRequest signUpRequest) {

        // Check if the user already exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email is already taken");
        }

        // Check if the email ends with @socio.com to determine if it's an admin
        boolean isAdmin = signUpRequest.getEmail().endsWith("@socio.com");

        // Create new user
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .isAdmin(isAdmin)
                .isProfilePrivate(false)
                .dateOfBirth(signUpRequest.getDateOfBirth())
                .passwordUpdatedAt(new Date())
                .build();

        userRepository.save(user);

        Optional<User> newUser = userRepository.findByEmail(signUpRequest.getEmail());

        User created = newUser.orElseThrow(() -> new InternalError(" - Failed to create User with email: " + signUpRequest.getEmail()));

        return created;
    }

    /**
     * Login a user
     * 
     * @param loginRequest LoginRequest
     * @return AuthResponse
     */
    public AuthResponse loginUser(LoginRequest loginRequest) {
        // Find user first to check if password is expired
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        // Check if password is expired (older than 30 days)
        if (user.getPasswordUpdatedAt() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(user.getPasswordUpdatedAt());
            calendar.add(Calendar.DAY_OF_MONTH, 30);

            if (calendar.getTime().before(new Date())) {
                throw new RuntimeException("Password expired. Please reset your password.");
            }
        }

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(jwt);
    }

    /**
     * Reset user password
     * 
     * @param request PasswordResetRequest
     * @return User
     */
    public User resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(new Date());

        return userRepository.save(user);
    }
}
