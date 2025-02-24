package com.hashedin.huSpark.service;

import com.hashedin.huSpark.model.Post;
import com.hashedin.huSpark.model.User;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Post createPost(Long userId, String content, String fileUrl) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = Post.builder()
                .user(user)
                .content(content)
                .fileUrl(fileUrl)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .comments(0)
                .build();
        return postRepository.save(post);
    }

    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public void likePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
    }
}