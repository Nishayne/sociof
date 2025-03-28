package com.hashedin.huSpark.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.CommentRequestDto;
import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.entity.Like;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.repository.CommentRepository;
import com.hashedin.huSpark.repository.LikeRepository;
import com.hashedin.huSpark.repository.PostRepository;

/**
 * Service for interaction-related operations (likes, comments).
 */
@Service
public class InteractionService {

    private final Logger log = LoggerFactory.getLogger(InteractionService.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    /**
     * Like a post.
     * @param postId ID of post to like
     * @param userId ID of user liking the post
     * @return True if post was liked, false if already liked
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public boolean likePost(Long postId, Long userId) {
        log.info("InteractionService: likePost: PostId: {}, UserId: {}", postId, userId);

        // Check if user already liked the post
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            log.warn("User {} already liked post {}.", userId, postId);
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", postId);
                    return new ResourceNotFoundException("Post not found with id: " + postId);
                });

        User user = userService.findById(userId);

        // Create like
        Like like = Like.builder()
                .post(post)
                .user(user)
                .build();

        likeRepository.saveAndFlush(like); // Changed from save to saveAndFlush

        // Update post like count
        post.setLikes(post.getLikes() + 1);
        postRepository.saveAndFlush(post); // Changed from save to saveAndFlush

        log.info("User {} liked post {} successfully.", userId, postId);
        return true;
    }

    /**
     * Unlike a post.
     * @param postId ID of post to unlike
     * @param userId ID of user unliking the post
     * @return True if post was unliked, false if not liked
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public boolean unlikePost(Long postId, Long userId) {
        log.info("InteractionService: unlikePost: PostId: {}, UserId: {}", postId, userId);

        // Check if user liked the post
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            log.warn("User {} did not like post {}.", userId, postId);
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", postId);
                    return new ResourceNotFoundException("Post not found with id: " + postId);
                });

        // Delete like
        likeRepository.deleteByPostIdAndUserId(postId, userId);

        // Update post like count
        post.setLikes(Math.max(0, post.getLikes() - 1));
        postRepository.saveAndFlush(post); // Changed from save to saveAndFlush

        log.info("User {} unliked post {} successfully.", userId, postId);
        return true;
    }

    /**
     * Add a comment to a post.
     * @param postId ID of post to comment on
     * @param userId ID of user commenting
     * @param content Comment content
     * @return Created comment
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public Comment addComment(Long postId, Long userId, CommentRequestDto content) {
        log.info("InteractionService: addComment: PostId: {}, UserId: {}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", postId);
                    return new ResourceNotFoundException("Post not found with id: " + postId);
                });

        User user = userService.findById(userId);

        // Create comment
        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content.getContent())
                .build();

        Comment savedComment = commentRepository.saveAndFlush(comment); // Changed from save to saveAndFlush
        log.info("User {} added comment to post {} successfully.", userId, postId);
        return savedComment;
    }

    /**
     * Get comments for a post.
     * @param postId ID of post to get comments for
     * @return List of comments
     */
    public List<Comment> getCommentsByPost(Long postId) {
        log.info("InteractionService: getCommentsByPost: PostId: {}", postId);
        List<Comment> comments = commentRepository.findByPostId(postId);
        log.info("Found {} comments for post {}.", comments.size(), postId);
        return comments;
    }
}