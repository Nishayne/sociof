package com.hashedin.huSpark.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.repository.CommentRepository;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.UserRepository;

@Service
public class CommentService {
    private final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long userId, Long postId, String content) {
        log.info("CommentService: addComment: UserId: {}, PostId: {}", userId, postId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found with id: {}", userId);
            return new RuntimeException("User not found");
        });
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("Post not found with id: {}", postId);
            return new RuntimeException("Post not found");
        });

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        post.getComments().add(comment);
        postRepository.saveAndFlush(post); // Changed from save to saveAndFlush
        Comment savedComment = commentRepository.saveAndFlush(comment); // Changed from save to saveAndFlush

        log.info("Comment added successfully: CommentId: {}", savedComment.getId());
        return savedComment;
    }

    public List<Comment> getCommentsByPost(Long postId) {
        log.info("CommentService: getCommentsByPost: PostId: {}", postId);
        List<Comment> comments = commentRepository.findByPostId(postId);
        log.info("Comments found for PostId {}: {}", postId, comments.size());
        return comments;
    }
}