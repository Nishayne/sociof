package com.hashedin.huSpark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.dto.LoginRequest;
import com.hashedin.huSpark.dto.PasswordResetRequest;
import com.hashedin.huSpark.dto.AuthResponse;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.UserAlreadyExistsException;
import com.hashedin.huSpark.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private SignUpRequest request;
    private LoginRequest loginRequest;
    private PasswordResetRequest passwordResetRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        request = SignUpRequest.builder()
                .email("test@example.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();
        
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        
        passwordResetRequest = PasswordResetRequest.builder()
                .email("test@example.com")
                .newPassword("newSecurePassword")
                .build();
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .isAdmin(false)
                .isProfilePrivate(false)
                .dateOfBirth(request.getDateOfBirth())
                .build();

        when(authService.registerUser(any(SignUpRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() throws Exception {
        when(authService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testLoginUser_Success() throws Exception {
        AuthResponse authResponse = new AuthResponse("validToken");
        when(authService.loginUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("validToken"));
    }

    @Test
    public void testLoginUser_InvalidCredentials() throws Exception {
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testResetPassword_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        when(authService.resetPassword(any(PasswordResetRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testResetPassword_InvalidEmail() throws Exception {
        when(authService.resetPassword(any(PasswordResetRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid email"));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnexpectedExceptionHandling() throws Exception {
        when(authService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
