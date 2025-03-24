package com.hashedin.huSpark.controller;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.dto.AuthResponse;
import com.hashedin.huSpark.dto.LoginRequest;
import com.hashedin.huSpark.dto.PasswordResetRequest;
import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.dto.UserDto;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.service.AuthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

/**
 * Controller for authentication operations.
 * Handles user registration, login, and password reset.
 */
@RestController
@RequestMapping("/api/auth")
@Api(tags = "Authentication")
public class AuthController {

    private final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * Constructor for AuthController.
     * @param authService Service for authentication operations.
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     * @param signUpRequest User registration request (contains email, password, etc.).
     * @return ResponseEntity containing the registered user's data (UserDto) or an error response.
     */
    @PostMapping("/signup")
    @ApiOperation("Register a new user")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("AuthController: signup"); // Log the signup request

        try {
            // Attempt to register the user
            User createdUser = authService.registerUser(signUpRequest);

            // Convert the User entity to UserDto using ModelMapper
            ModelMapper modelMapper = new ModelMapper();
            UserDto userDto = modelMapper.map(createdUser, UserDto.class);

            // Return a 201 Created response with the UserDto
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);

        } catch (IllegalArgumentException e) {
            // Handle cases where the email already exists or other validation issues
            log.warn("Signup failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            // Handle other unexpected exceptions
            log.error("Signup failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Logs in a user.
     * @param loginRequest User login request (contains email and password).
     * @return ResponseEntity containing the authentication token (AuthResponse) or an error response.
     */
    @PostMapping("/login")
    @ApiOperation("Login a user")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("AuthController: login"); // Log the login request

        try {
            // Attempt to log in the user
            AuthResponse authResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(authResponse); // Return a 200 OK response with the authentication token

        } catch (IllegalArgumentException e) {
            // Handle invalid credentials or other login-related errors
            log.warn("Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            // Handle other unexpected exceptions during login
            log.error("Login failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Resets a user's password.
     * @param request Password reset request (contains email and new password).
     * @return ResponseEntity containing the updated user's data (UserDto) or an error response.
     */
    @PostMapping("/reset-password")
    @ApiOperation("Reset user password")
    public ResponseEntity<UserDto> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("AuthController: reset-pass"); // Log the password reset request

        try {
            // Attempt to reset the user's password
            User resetUser = authService.resetPassword(request);

            // Convert the User entity to UserDto using ModelMapper
            ModelMapper modelMapper = new ModelMapper();
            UserDto userDto = modelMapper.map(resetUser, UserDto.class);

            return ResponseEntity.ok(userDto); // Return a 200 OK response with the updated user data

        } catch (IllegalArgumentException e) {
            // Handle invalid password reset requests (e.g., invalid email)
            log.warn("Password reset failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            // Handle other unexpected exceptions during password reset
            log.error("Password reset failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}