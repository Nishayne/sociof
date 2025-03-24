package com.hashedin.huSpark.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hashedin.huSpark.dto.UserDto;
import com.hashedin.huSpark.dto.UserUpdateRequest;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

/**
 * Controller for user operations.
 * Handles retrieving, updating, deleting, and bulk importing users, as well as retrieving user statistics.
 */
@RestController
@RequestMapping("/api/users")
@Api(tags = "Users")
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Constructor for UserController.
     * @param userService Service for user operations.
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the current user.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the UserDto or an error response.
     */
    @GetMapping("/me")
    @ApiOperation("Get current user")
    public ResponseEntity<UserDto> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        log.info("UserController: currentUser ID:" + currentUser.getId() + " - email:" + currentUser.getEmail());
        try {
            User user = userService.findById(currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
        } catch (Exception e) {
            log.error("Failed to get current user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a user by their ID.
     * @param id User ID.
     * @return ResponseEntity containing the UserDto or an error response.
     */
    @GetMapping("/{id}")
    @ApiOperation("Get a user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("UserController: userID: " + id);
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
        } catch (Exception e) {
            log.error("Failed to get user by ID: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates a user.
     * @param id User ID.
     * @param updateRequest User update request.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the updated UserDto or an error response.
     */
    @PutMapping("/{id}")
    @ApiOperation("Update a user")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("UserController: updateUser: " + id);
        try {
            User updatedUser = userService.updateUser(id, updateRequest, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(updatedUser, UserDto.class));
        } catch (Exception e) {
            log.error("Failed to update user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Deletes a user.
     * @param id User ID.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity indicating successful deletion or an error response.
     */
    @DeleteMapping("/{id}")
    @ApiOperation("Delete a user")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        log.info("UserController: deleteUser: " + id);
        try {
            userService.deleteUser(id, currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all users.
     * @param searchTerm Search term for filtering.
     * @param pageable Pagination parameters.
     * @return ResponseEntity containing a page of UserDto or an error response.
     */
    @GetMapping
    @ApiOperation("Get all users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        log.info("UserController: searchTerm: " + searchTerm);
        try {
            Page<User> users = userService.getAllUsers(searchTerm, pageable);
            Page<UserDto> userDtos = users.map(user -> modelMapper.map(user, UserDto.class));
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            log.error("Failed to get all users: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Bulk imports users.
     * @param file CSV file.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing a list of UserDto or an error response.
     */
    @PostMapping("/bulk-import")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Bulk import users (Admin only)")
    public ResponseEntity<List<UserDto>> bulkImportUsers(
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal currentUser) {
        log.info("UserController: bulkImportUsers: fileSize: " + file.getSize());
        try {
            List<User> importedUsers = userService.bulkImportUsers(file, currentUser.getId());
            List<UserDto> userDtos = importedUsers.stream()
                    .map(user -> modelMapper.map(user, UserDto.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            log.error("Failed to bulk import users: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves users ordered by follower count.
     * @param pageable Pagination parameters.
     * @return ResponseEntity containing a page of Object[] or an error response.
     */
    @GetMapping("/stats/followers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get users ordered by follower count (Admin only)")
    public ResponseEntity<Page<Object[]>> getUsersOrderedByFollowerCount(Pageable pageable) {
        log.info("UserController: getUsersOrderedByFollowerCount: pageSize: " + pageable.getPageSize());
        try {
            Page<Object[]> users = userService.getUsersOrderedByFollowerCount(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to get users ordered by follower count: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}