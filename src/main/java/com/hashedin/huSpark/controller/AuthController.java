package com.hashedin.huSpark.controller;

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
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.service.AuthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

import com.hashedin.huSpark.dto.UserDto;
import org.modelmapper.ModelMapper;

/**
 * Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@Api(tags = "Authentication")
public class AuthController {

    Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user
     * @param signUpRequest User registration request
     * @return Registered user
     */
    @PostMapping("/signup")
    @ApiOperation("Register a new user")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {

        log.info("AuthController: signup");
        try {
            User createdUser = authService.registerUser(signUpRequest);

            ModelMapper modelMapper = new ModelMapper();
            UserDto userDto = modelMapper.map(createdUser, UserDto.class);  // User user = modelMapper.map(userDto, User.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto); // Return 201 Created
        } catch (IllegalArgumentException e) {
            // Handle cases where the email already exists or other validation issues
            log.warn("Signup failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // or .body(e.getMessage())
        } catch (Exception e) {
            // Handle other unexpected exceptions
            log.error("Signup failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Login a user
     * @param loginRequest User login request
     * @return Authentication token
     */
    @PostMapping("/login")
    @ApiOperation("Login a user")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("AuthController: login");

        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }

    /**
     * Reset user password
     * @param request Password reset request
     * @return User with reset password
     */
    @PostMapping("/reset-password")
    @ApiOperation("Reset user password")
    public ResponseEntity<User> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("AuthController: reset-pass");

        return ResponseEntity.ok(authService.resetPassword(request));
    }
}