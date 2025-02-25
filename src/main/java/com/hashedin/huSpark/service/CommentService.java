package com.hashedin.huSpark.service;

import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.repository.CommentRepository;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long userId, Long postId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        post.getComments().add(comment);
        postRepository.save(post);

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }
}