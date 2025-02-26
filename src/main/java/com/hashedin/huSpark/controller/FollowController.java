package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.FollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for follow operations
 */
@RestController
@RequestMapping("/api/follows")
@Api(tags = "Follows")
public class FollowController {

    Logger log = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    /**
     * Follow a user
     * @param userId User ID to follow
     * @param currentUser Current authenticated user
     * @return Success message
     */
    @PostMapping("/{userId}")
    @ApiOperation("Follow a user")
    public ResponseEntity<?> followUser(
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("FollowController: follow : UserID: " + userId);

        boolean followed = followService.followUser(currentUser.getId(), userId);
        return ResponseEntity.ok(followed ? "User followed" : "User already followed");
    }

    /**
     * Unfollow a user
     * @param userId User ID to unfollow
     * @param currentUser Current authenticated user
     * @return Success message
     */
    @DeleteMapping("/{userId}")
    @ApiOperation("Unfollow a user")
    public ResponseEntity<?> unfollowUser(
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("FollowController: Unfollow : UserID: " + userId);

        boolean unfollowed = followService.unfollowUser(currentUser.getId(), userId);
        return ResponseEntity.ok(unfollowed ? "User unfollowed" : "User was not followed");
    }

    /**
     * Get follower count for a user
     * @param userId User ID
     * @return Follower count
     */
    @GetMapping("/{userId}/followers/count")
    @ApiOperation("Get follower count for a user")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Long userId) {
        log.info("FollowController: followerCount : UserID: " + userId);

        long count = followService.getFollowerCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get following count for a user
     * @param userId User ID
     * @return Following count
     */
    @GetMapping("/{userId}/following/count")
    @ApiOperation("Get following count for a user")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        log.info("FollowController: followingCount : UserID: " + userId);

        long count = followService.getFollowingCount(userId);
        return ResponseEntity.ok(count);
    }
}