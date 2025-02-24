package com.hashedin.huSpark.service;

import com.hashedin.huSpark.model.Post;
import com.hashedin.huSpark.model.SharedPost;
import com.hashedin.huSpark.model.User;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.SharedPostRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SharedPostService {
    private final SharedPostRepository sharedPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public SharedPostService(SharedPostRepository sharedPostRepository, PostRepository postRepository, UserRepository userRepository) {
        this.sharedPostRepository = sharedPostRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public SharedPost sharePost(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post originalPost = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        SharedPost sharedPost = SharedPost.builder()
                .user(user)
                .originalPost(originalPost)
                .sharedAt(LocalDateTime.now())
                .build();

        return sharedPostRepository.save(sharedPost);
    }

    public List<SharedPost> getSharedPostsByUser(Long userId) {
        return sharedPostRepository.findByUserId(userId);
    }
}
