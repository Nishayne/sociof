package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class FollowController {
    private final FollowService followService;
    public FollowController(FollowService followService){
        this.followService=followService;
    }

    @PostMapping("/follow")
    public ResponseEntity<String> followUser(@RequestBody Map<String,Long> request){
        followService.followUser(request.get("followerId"),request.get("followingId"));
        return ResponseEntity.ok("User followed successfully");
    }

    @PostMapping("/unfollow")
    public ResponseEntity<String> unfollowUser(@RequestBody Map<String,Long> request){
        followService.unfollowUser(request.get("followerId"),request.get("followingId"));
        return ResponseEntity.ok("User unfollowed successfully");
    }

    @PostMapping("/followers/{userId}")
    public ResponseEntity<List<Long>>getFollowers(@PathVariable Long userId){
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @PostMapping("/following/{userId}")
    public ResponseEntity<List<Long>>getFollowing(@PathVariable Long userId){
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

}
