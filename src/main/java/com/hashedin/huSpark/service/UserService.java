package com.hashedin.huSpark.service;

import com.hashedin.huSpark.dto.UserDto;
import com.hashedin.huSpark.dto.UserUpdateRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UnauthorizedException;
import com.hashedin.huSpark.repository.FollowRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for user-related operations
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Create a new user
     * @param user User to create
     * @return Created user
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPasswordUpdatedAt(new Date());
        return userRepository.save(user);
    }

    /**
     * Find a user by email
     * @param email Email to search for
     * @return Optional user
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user by ID
     * @param id ID to search for
     * @return User
     * @throws ResourceNotFoundException if user is not found
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Update a user
     * @param id ID of user to update
     * @param updateRequest User update request
     * @param currentUserId ID of current authenticated user
     * @return Updated user
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User updateUser(Long id, UserUpdateRequest updateRequest, Long currentUserId) {
        User user = findById(id);

        // Check if current user is the user being updated or an admin
        User currentUser = findById(currentUserId);
        if (!currentUser.getIsAdmin() && !currentUserId.equals(id)) {
            throw new UnauthorizedException("You do not have permission to update this user");
        }

        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getIsProfilePrivate() != null) {
            user.setIsProfilePrivate(updateRequest.getIsProfilePrivate());
        }

        if (updateRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(updateRequest.getDateOfBirth());
        }

        return userRepository.save(user);
    }

    /**
     * Delete a user
     * @param id ID of user to delete
     * @param currentUserId ID of current authenticated user
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id, Long currentUserId) {
        User user = findById(id);

        // Check if current user is the user being deleted or an admin
        User currentUser = findById(currentUserId);
        if (!currentUser.getIsAdmin() && !currentUserId.equals(id)) {
            throw new UnauthorizedException("You do not have permission to delete this user");
        }

        userRepository.delete(user);
    }

    /**
     * Get all users
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @Cacheable(value = "users", key = "'search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getAllUsers(String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return userRepository.searchUsers(searchTerm, pageable);
        }
        return userRepository.findAll(pageable);
    }

    /**
     * Bulk import users from CSV
     * @param file CSV file
     * @param currentUserId ID of current authenticated user
     * @return List of imported users
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public List<User> bulkImportUsers(MultipartFile file, Long currentUserId) {
        // Check if current user is an admin
        User currentUser = findById(currentUserId);
        if (!currentUser.getIsAdmin()) {
            throw new UnauthorizedException("Only admins can bulk import users");
        }

        List<User> importedUsers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header line
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                // Extract data
                String email = data[0].trim();
                String password = data[1].trim();
                boolean isAdmin = Boolean.parseBoolean(data[2].trim());

                // Skip existing users
                if (userRepository.existsByEmail(email)) {
                    continue;
                }

                // Create and save user
                User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .isAdmin(isAdmin)
                        .isProfilePrivate(false)
                        .passwordUpdatedAt(new Date())
                        .build();

                importedUsers.add(userRepository.save(user));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }

        return importedUsers;
    }

    /**
     * Get users ordered by follower count
     * @param pageable Pagination parameters
     * @return Page of user DTOs with follower count
     */
    @Cacheable(value = "userStats", key = "'followerCount_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Object[]> getUsersOrderedByFollowerCount(Pageable pageable) {
        return followRepository.findUsersOrderedByFollowerCount(pageable);
    }
}
