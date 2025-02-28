package com.hashedin.huSpark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthController
 */
@WebMvcTest(AuthController.class)
//@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;


    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

  
    @Test
    public void testRegisterUser_Success() throws Exception {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .isAdmin(false)
                .isProfilePrivate(false)
                .dateOfBirth(request.getDateOfBirth())
                .build();

        when(authService.registerUser(any(SignUpRequest.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    public void testRegisterUser_InvalidEmail() throws Exception {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("invalid-email")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterUser_InvalidPassword() throws Exception {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .password("12345") // Too short
                .dateOfBirth(new Date())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
