package com.hashedin.huSpark.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.entity.Follow;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.repository.FollowRepository;

/**
 * Service for follow-related operations.
 */
@Service
public class FollowService {

    private final Logger log = LoggerFactory.getLogger(FollowService.class);

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserService userService;

    /**
     * Follow a user.
     * @param followerId ID of follower
     * @param followingId ID of user to follow
     * @return True if follow was created, false if already following
     */
    @Transactional
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean followUser(Long followerId, Long followingId) {
        log.info("FollowService: followUser: FollowerId: {}, FollowingId: {}", followerId, followingId);

        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            log.warn("User {} is already following user {}.", followerId, followingId);
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

        followRepository.saveAndFlush(follow); // Changed from save to saveAndFlush
        log.info("User {} followed user {} successfully.", followerId, followingId);
        return true;
    }

    /**
     * Unfollow a user.
     * @param followerId ID of follower
     * @param followingId ID of user to unfollow
     * @return True if unfollow was successful, false if not following
     */
    @Transactional
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public boolean unfollowUser(Long followerId, Long followingId) {
        log.info("FollowService: unfollowUser: FollowerId: {}, FollowingId: {}", followerId, followingId);

        // Check if following
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            log.warn("User {} is not following user {}.", followerId, followingId);
            return false;
        }

        // Delete follow relationship
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        log.info("User {} unfollowed user {} successfully.", followerId, followingId);
        return true;
    }

    /**
     * Get follower count for a user.
     * @param userId ID of user
     * @return Number of followers
     */
    public long getFollowerCount(Long userId) {
        log.info("FollowService: getFollowerCount: UserId: {}", userId);

        // Check if user exists
        userService.findById(userId);
        long count = followRepository.countFollowersByUserId(userId);
        log.info("Follower count for user {}: {}", userId, count);
        return count;
    }

    /**
     * Get following count for a user.
     * @param userId ID of user
     * @return Number of users being followed
     */
    public long getFollowingCount(Long userId) {
        log.info("FollowService: getFollowingCount: UserId: {}", userId);

        // Check if user exists
        userService.findById(userId);
        long count = followRepository.countFollowingByUserId(userId);
        log.info("Following count for user {}: {}", userId, count);
        return count;
    }
}