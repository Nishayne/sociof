package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.dto.UserDto;
import com.hashedin.huSpark.dto.UserUpdateRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for user operations
 */
@RestController
@RequestMapping("/api/users")
@Api(tags = "Users")
public class UserController {

    Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Get current user
     * @param currentUser Current authenticated user
     * @return Current user
     */
    @GetMapping("/me")
    @ApiOperation("Get current user")
    public ResponseEntity<User> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        log.info("currentUser ID:"+currentUser.getId() + " - email:" + currentUser.getEmail());
        User user = userService.findById(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Get a user by ID
     * @param id User ID
     * @return User
     */
    @GetMapping("/{id}")
    @ApiOperation("Get a user by ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update a user
     * @param id User ID
     * @param updateRequest User update request
     * @param currentUser Current authenticated user
     * @return Updated user
     */
    @PutMapping("/{id}")
    @ApiOperation("Update a user")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            @CurrentUser UserPrincipal currentUser) {
        User updatedUser = userService.updateUser(id, updateRequest, currentUser.getId());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete a user
     * @param id User ID
     * @param currentUser Current authenticated user
     * @return Deleted user
     */
    @DeleteMapping("/{id}")
    @ApiOperation("Delete a user")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        userService.deleteUser(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Get all users
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of users
     */
    @GetMapping
    @ApiOperation("Get all users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<User> users = userService.getAllUsers(searchTerm, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Bulk import users
     * @param file CSV file
     * @param currentUser Current authenticated user
     * @return List of imported users
     */
    @PostMapping("/bulk-import")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Bulk import users (Admin only)")
    public ResponseEntity<List<User>> bulkImportUsers(
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal currentUser) {
        List<User> importedUsers = userService.bulkImportUsers(file, currentUser.getId());
        return ResponseEntity.ok(importedUsers);
    }

    /**
     * Get users ordered by follower count
     * @param pageable Pagination parameters
     * @return Page of users with follower count
     */
    @GetMapping("/stats/followers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get users ordered by follower count (Admin only)")
    public ResponseEntity<Page<Object[]>> getUsersOrderedByFollowerCount(Pageable pageable) {
        Page<Object[]> users = userService.getUsersOrderedByFollowerCount(pageable);
        return ResponseEntity.ok(users);
    }
}