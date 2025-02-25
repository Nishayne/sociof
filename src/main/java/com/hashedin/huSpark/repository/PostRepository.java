package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Post entity
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUserId(Long userId, Pageable pageable);

    Page<Post> findByGroupId(Long groupId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Post> searchUserPosts(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId AND LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Post> searchGroupPosts(@Param("groupId") Long groupId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.user.isProfilePrivate = false OR p.user.id = :currentUserId OR p.user.id IN " +
            "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :currentUserId)) AND " +
            "(p.group IS NULL OR p.group.isPrivate = false OR p.group.id IN " +
            "(SELECT g.id FROM Group g JOIN g.members m WHERE m.id = :currentUserId))")
    Page<Post> findVisiblePosts(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query("SELECT DATE(p.createdAt) as date, COUNT(p) as count FROM Post p GROUP BY DATE(p.createdAt) ORDER BY date")
    List<Object[]> countPostsByCreationDate();

    @Query("SELECT p.user.id as userId, COUNT(p) as count FROM Post p GROUP BY p.user.id ORDER BY count DESC")
    List<Object[]> countPostsByUser();

    @Query("SELECT p.fileType as fileType, COUNT(p) as count FROM Post p WHERE p.fileType IS NOT NULL GROUP BY p.fileType ORDER BY count DESC")
    List<Object[]> countPostsByFileType();

    @Query("SELECT p FROM Post p ORDER BY p.likes DESC, SIZE(p.comments) DESC")
    Page<Post> findPostsOrderedByEngagement(Pageable pageable);
}
