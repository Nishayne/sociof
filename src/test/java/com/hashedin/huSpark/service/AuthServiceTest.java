package com.hashedin.huSpark.service;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hashedin.huSpark.dto.SignUpRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.UserAlreadyExistsException;
import com.hashedin.huSpark.repository.UserRepository;

/**
 * Unit tests for the AuthService
 */
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("test@example.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> { // Changed from save to saveAndFlush
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulating DB-generated ID
            return savedUser;
        });

        // Act
        User result = authService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertNotNull(result.getIsAdmin()); // Ensure isAdmin is not null
        assertFalse(result.getIsAdmin());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUser_AdminEmail() {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("admin@socio.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> { // Changed from save to saveAndFlush
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L); // Simulating DB-generated ID
            return savedUser;
        });

        // Act
        User result = authService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("admin@socio.com", result.getEmail());
        assertTrue(result.getIsAdmin());

        verify(userRepository).existsByEmail("admin@socio.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .dateOfBirth(new Date())
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));

        verify(userRepository).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}