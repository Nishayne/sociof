package com.hashedin.huSpark.service;

import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.entity.Like;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.repository.CommentRepository;
import com.hashedin.huSpark.repository.LikeRepository;
import com.hashedin.huSpark.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for interaction-related operations (likes, comments)
 */
@Service
public class InteractionService {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    /**
     * Like a post
     * @param postId ID of post to like
     * @param userId ID of user liking the post
     * @return True if post was liked, false if already liked
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public boolean likePost(Long postId, Long userId) {
        // Check if user already liked the post
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userService.findById(userId);

        // Create like
        Like like = Like.builder()
                .post(post)
                .user(user)
                .build();

        likeRepository.save(like);

        // Update post like count
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);

        return true;
    }

    /**
     * Unlike a post
     * @param postId ID of post to unlike
     * @param userId ID of user unliking the post
     * @return True if post was unliked, false if not liked
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public boolean unlikePost(Long postId, Long userId) {
        // Check if user liked the post
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Delete like
        likeRepository.deleteByPostIdAndUserId(postId, userId);

        // Update post like count
        post.setLikes(Math.max(0, post.getLikes() - 1));
        postRepository.save(post);

        return true;
    }

    /**
     * Add a comment to a post
     * @param postId ID of post to comment on
     * @param userId ID of user commenting
     * @param content Comment content
     * @return Created comment
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public Comment addComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userService.findById(userId);

        // Create comment
        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();

        return commentRepository.save(comment);
    }

    /**
     * Get comments for a post
     * @param postId ID of post to get comments for
     * @return List of comments
     */
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }
}
