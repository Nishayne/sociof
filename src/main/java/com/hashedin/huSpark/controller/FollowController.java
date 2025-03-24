package com.hashedin.huSpark.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.FollowService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller for follow operations.
 * Handles following and unfollowing users, and retrieving follower/following counts.
 */
@RestController
@RequestMapping("/api/follows")
@Api(tags = "Follows")
public class FollowController {

    private final Logger log = LoggerFactory.getLogger(FollowController.class);
    private final FollowService followService;

    /**
     * Constructor for FollowController.
     * @param followService Service for follow operations.
     */
    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * Follows a user.
     * @param userId User ID to follow.
     * @param currentUser Current authenticated user (follower).
     * @return ResponseEntity containing a success message or an error response.
     */
    @PostMapping("/{userId}")
    @ApiOperation("Follow a user")
    public ResponseEntity<?> followUser(
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {

        log.info("FollowController: follow : UserID: " + userId);

        try {
            boolean followed = followService.followUser(currentUser.getId(), userId);
            return ResponseEntity.ok(followed ? "User followed" : "User already followed");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to follow user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to follow user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Unfollows a user.
     * @param userId User ID to unfollow.
     * @param currentUser Current authenticated user (unfollower).
     * @return ResponseEntity containing a success message or an error response.
     */
    @DeleteMapping("/{userId}")
    @ApiOperation("Unfollow a user")
    public ResponseEntity<?> unfollowUser(
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("FollowController: Unfollow : UserID: " + userId);

        try {
            boolean unfollowed = followService.unfollowUser(currentUser.getId(), userId);
            return ResponseEntity.ok(unfollowed ? "User unfollowed" : "User was not followed");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to unfollow user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to unfollow user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves the follower count for a user.
     * @param userId User ID.
     * @return ResponseEntity containing the follower count or an error response.
     */
    @GetMapping("/{userId}/followers/count")
    @ApiOperation("Get follower count for a user")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Long userId) {
        log.info("FollowController: followerCount : UserID: " + userId);

        try {
            long count = followService.getFollowerCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get follower count: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves the following count for a user.
     * @param userId User ID.
     * @return ResponseEntity containing the following count or an error response.
     */
    @GetMapping("/{userId}/following/count")
    @ApiOperation("Get following count for a user")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        log.info("FollowController: followingCount : UserID: " + userId);

        try {
            long count = followService.getFollowingCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get following count: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}