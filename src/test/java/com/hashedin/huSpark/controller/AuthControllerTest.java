package com.hashedin.huSpark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.dto.UserDto;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.UserAlreadyExistsException;
import com.hashedin.huSpark.repository.UserRepository;
import com.hashedin.huSpark.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthController
 */
// @SpringBootTest
//@AutoConfigureMockMvc
/*@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")  // Loads in-memory DB settings
@Import(AuthService.class)  // Ensures AuthService is loaded
@MockBean(AuthService.class) 
@MockBean(UserRepository.class)  // Prevents real DB calls
@MockBean(JpaRepositoriesAutoConfiguration.class) // Prevents DB loading*/
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;


    @MockBean
    private UserRepository userRepository; // Mock the database dependency

    @InjectMocks
    private AuthController authController;

    /*@Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }*/

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testRegisterUser2_Success() {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .isAdmin(false)
                .build();

        when(authService.registerUser(any(SignUpRequest.class))).thenReturn(mockUser);

        // Act
        ResponseEntity<UserDto> response = authController.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        when(authService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        // Act & Assert
        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> authController.registerUser(request));
        assertEquals("User already exists", exception.getMessage());
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

        Mockito.when(authService.registerUser(Mockito.any(SignUpRequest.class)))
               .thenThrow(new RuntimeException("Invalid password"));
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
