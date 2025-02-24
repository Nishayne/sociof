package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.model.SharedPost;
import com.hashedin.huSpark.service.SharedPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shared-posts")
public class SharedPostController {
    private final SharedPostService sharedPostService;
    public SharedPostController(SharedPostService sharedPostService){
        this.sharedPostService=sharedPostService;
    }

    @PostMapping("/share")
    public ResponseEntity<SharedPost> sharePost(@RequestBody Map<String,String> request){
        Long userId=Long.parseLong(request.get("userId"));
        Long postId= Long.parseLong(request.get("postId"));
        SharedPost sharedPost=sharedPostService.sharePost(userId,postId);
        return ResponseEntity.ok(sharedPost);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SharedPost>> getSharedPostsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(sharedPostService.getSharedPostsByUser(userId));
    }
}
