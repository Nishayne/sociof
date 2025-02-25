package com.hashedin.huSpark.service;

import com.hashedin.huSpark.entity.Follow;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for follow-related operations
 */
@Service
public class FollowService {
    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserService userService;

    /**
     * Follow a user
     * @param followerId ID of follower
     * @param followingId ID of user to follow
     * @return True if follow was created, false if already following
     */
    @Transactional
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean followUser(Long followerId, Long followingId) {
        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return false;
        }

        // Check if users exist
        User follower = userService.findById(followerId);
        User following = userService.findById(followingId);

        // Create follow relationship
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
        return true;
    }

    /**
     * Unfollow a user
     * @param followerId ID of follower
     * @param followingId ID of user to unfollow
     * @return True if unfollow was successful, false if not following
     */
    @Transactional
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean unfollowUser(Long followerId, Long followingId) {
        // Check if following
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return false;
        }

        // Delete follow relationship
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        return true;
    }

    /**
     * Get follower count for a user
     * @param userId ID of user
     * @return Number of followers
     */
    public long getFollowerCount(Long userId) {
        // Check if user exists
        userService.findById(userId);
        return followRepository.countFollowersByUserId(userId);
    }

    /**
     * Get following count for a user
     * @param userId ID of user
     * @return Number of users being followed
     */
    public long getFollowingCount(Long userId) {
        // Check if user exists
        userService.findById(userId);
        return followRepository.countFollowingByUserId(userId);
    }
}