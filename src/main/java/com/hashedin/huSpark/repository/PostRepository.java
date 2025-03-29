package com.hashedin.huSpark.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.PostCreationDateDTO;
import com.hashedin.huSpark.dto.PostFileTypeCountDTO;
import com.hashedin.huSpark.dto.UserPostCountDTO;
import com.hashedin.huSpark.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"comments", "postLikes"})
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"comments", "postLikes"})
    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId")
    Page<Post> findByGroupId(Long groupId, Pageable pageable);

    // Search posts by a specific user with a search term
    @Query("""
        SELECT p FROM Post p 
        WHERE p.user.id = :userId AND LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
        ORDER BY p.createdAt DESC
        """)
    Page<Post> searchUserPosts(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Search posts within a specific group with a search term
    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId AND LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY p.createdAt DESC")
    Page<Post> searchGroupPosts(@Param("groupId") Long groupId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Fetch only visible posts for a user (Optimized to avoid subqueries)
    @EntityGraph(attributePaths = {"comments", "postLikes"})
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.group WHERE (p.user.isProfilePrivate = false " +
            "OR p.user.id = :currentUserId OR p.user.id IN " +
            "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :currentUserId)) AND " +
            "(p.group IS NULL OR p.group.isPrivate = false OR p.group.id IN " +
            "(SELECT g.id FROM Group g JOIN g.members m WHERE m.id = :currentUserId)) ORDER BY p.createdAt DESC")
    /*
           """
           SELECT p FROM Post p 
           LEFT JOIN FETCH p.user 
           LEFT JOIN FETCH p.group 
           WHERE (p.user.isProfilePrivate = false OR p.user.id = :currentUserId 
                  OR EXISTS (SELECT 1 FROM Follow f WHERE f.follower.id = :currentUserId AND f.following.id = p.user.id)) 
           AND (p.group IS NULL OR p.group.isPrivate = false 
                  OR EXISTS (SELECT 1 FROM Group g JOIN g.members m WHERE g.id = p.group.id AND m.id = :currentUserId))
           ORDER BY p.createdAt DESC
           """ 
    */
    Page<Post> findVisiblePosts(@Param("currentUserId") Long currentUserId, Pageable pageable);

    // Count posts grouped by creation date (Avoids DATE function issue in some databases)
    /*
           """
           SELECT new com.hashedin.huSpark.dto.PostCreationDateDTO(FUNCTION('DATE', p.createdAt), COUNT(p)) 
           FROM Post p 
           GROUP BY FUNCTION('DATE', p.createdAt) 
           ORDER BY FUNCTION('DATE', p.createdAt)
           """ 
     */
    @Query("SELECT new com.hashedin.huSpark.dto.PostCreationDateDTO(DATE(p.createdAt), COUNT(p)) FROM Post p GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<PostCreationDateDTO> countPostsByCreationDate();

    // Count number of posts per user (Ordered by post count descending)
    @Query("SELECT new com.hashedin.huSpark.dto.UserPostCountDTO(p.user.id, COUNT(p)) FROM Post p GROUP BY p.user.id ORDER BY COUNT(p) DESC")
    List<UserPostCountDTO> countPostsByUser();

    // Count number of posts by file type (Ensuring only non-null file types are counted)
    @Query("SELECT new com.hashedin.huSpark.dto.PostFileTypeCountDTO(p.fileType, COUNT(p)) FROM Post p WHERE p.fileType IS NOT NULL GROUP BY p.fileType ORDER BY COUNT(p) DESC")
    List<PostFileTypeCountDTO> countPostsByFileType();

    // Find posts ordered by engagement (likes and comments count)
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.group ORDER BY p.likes DESC, SIZE(p.comments) DESC")
    Page<Post> findPostsOrderedByEngagement(Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteCommentsByPostId(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Like l WHERE l.post.id = :postId")
    void deleteLikesByPostId(@Param("postId") Long postId);


    @Modifying
    @Transactional
    @Query("DELETE FROM Report r WHERE r.post.id = :postId")
    void deleteReportsByPostId(@Param("postId") Long postId);
}