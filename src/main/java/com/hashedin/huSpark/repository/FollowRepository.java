package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * Repository for Follow entity
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    long countFollowersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countFollowingByUserId(@Param("userId") Long userId);

    @Query("SELECT u, COUNT(f) as followerCount FROM User u LEFT JOIN Follow f ON u.id = f.following.id " +
            "GROUP BY u ORDER BY followerCount DESC")
    Page<Object[]> findUsersOrderedByFollowerCount(Pageable pageable);

    @Query("SELECT DATE(f.createdAt) as date, COUNT(f) as count FROM Follow f GROUP BY DATE(f.createdAt) ORDER BY date")
    List<Object[]> countFollowsByCreationDate();
}
