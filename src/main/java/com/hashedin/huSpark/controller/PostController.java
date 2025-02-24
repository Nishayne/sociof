package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.model.Post;
import com.hashedin.huSpark.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String content = request.get("content");
        String fileUrl = request.get("fileUrl");
        Post post = postService.createPost(userId, content, fileUrl);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @PostMapping("/like/{postId}")
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        postService.likePost(postId);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }
}
