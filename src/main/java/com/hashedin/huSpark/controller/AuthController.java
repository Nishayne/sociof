package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.dto.AuthResponse;
import com.hashedin.huSpark.dto.LoginRequest;
import com.hashedin.huSpark.dto.PasswordResetRequest;
import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@Api(tags = "Authentication")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user
     * @param signUpRequest User registration request
     * @return Registered user
     */
    @PostMapping("/signup")
    @ApiOperation("Register a new user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }

    /**
     * Login a user
     * @param loginRequest User login request
     * @return Authentication token
     */
    @PostMapping("/login")
    @ApiOperation("Login a user")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
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
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}