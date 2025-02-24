package com.hashedin.huSpark.service;

import com.hashedin.huSpark.model.Follow;
import com.hashedin.huSpark.model.User;
import com.hashedin.huSpark.repository.FollowRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    public FollowService(FollowRepository followRepository, UserRepository userRepository){
        this.followRepository=followRepository;
        this.userRepository=userRepository;
    }
    public void followUser(Long followerId, Long followingId){
        if(followRepository.existsByFollowerIdAndFollowingId(followerId,followingId)){
            throw new RuntimeException("Already following user.");
        }
        User follower=userRepository.findById(followerId).orElseThrow(() -> new RuntimeException("Follower not found."));
        User following=userRepository.findById(followingId).orElseThrow(() -> new RuntimeException("Following user not found."));
        Follow follow=new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
    }

    public void unfollowUser(Long followerId,Long followingId){
        if(!followRepository.existsByFollowerIdAndFollowingId(followerId,followingId)){
            throw new RuntimeException("Not following user");
        }
        followRepository.deleteByFollowerIdAndFollowingId(followerId,followingId);
    }

    public List<Long> getFollowers(Long userId){
        return followRepository.findByFollowingId(userId)
                .stream()
                .map(follow->follow.getFollower().getId())
                .collect(Collectors.toList());
    }

    public List<Long> getFollowing(Long userId){
        return followRepository.findByFollowerId(userId)
                .stream()
                .map(follow -> follow.getFollowing().getId())
                .collect(Collectors.toList());
    }
}
