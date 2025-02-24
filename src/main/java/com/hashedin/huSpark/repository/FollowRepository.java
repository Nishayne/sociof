package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow,Long> {
    List<Follow> findByFollowerId(Long followerId);
    List<Follow> findByFollowingId(Long followingId);
    void deleteByFollowerIdAndFollowingId(Long followerId,Long followingId);
    boolean existsByFollowerIdAndFollowingId(Long followerId,Long followingId);
}
