package com.hashedin.huSpark.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.PostRequest;
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UnauthorizedException;
import com.hashedin.huSpark.repository.CommentRepository;
import com.hashedin.huSpark.repository.GroupRepository;
import com.hashedin.huSpark.repository.LikeRepository;
import com.hashedin.huSpark.repository.PostRepository;

/**
 * Service for post-related operations
 */
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Create a new post
     * @param postRequest Post creation request
     * @param userId ID of user creating the post
     * @return Created post
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public Post createPost(PostRequest postRequest, Long userId) {
        User user = userService.findById(userId);

        Post post = Post.builder()
                .content(postRequest.getContent())
                .fileUrl(postRequest.getFileUrl())
                .fileType(postRequest.getFileType())
                .user(user)
                .isShared(false)
                .likes(0)
                .build();

        // If group ID is provided, check if user is a member of the group
        if (postRequest.getGroupId() != null) {
            Group group = groupRepository.findById(postRequest.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + postRequest.getGroupId()));

            // Check if user is a member of the group
            if (!group.getMembers().contains(user) && !group.getCreator().getId().equals(userId)) {
                throw new UnauthorizedException("You must be a member of the group to post");
            }

            post.setGroup(group);
        }

        return postRepository.save(post);
    }

    /**
     * Share an existing post
     * @param postId ID of post to share
     * @param userId ID of user sharing the post
     * @return Shared post
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public Post sharePost(Long postId, Long userId) {
        User user = userService.findById(userId);
        Post originalPost = findById(postId);

        // Create a new post as a share
        Post sharedPost = Post.builder()
                .content("Shared post: " + originalPost.getContent())
                .user(user)
                .isShared(true)
                .originalPostId(originalPost.getId())
                .originalUserId(originalPost.getUser().getId())
                .likes(0)
                .build();

        return postRepository.save(sharedPost);
    }

    /**
     * Find a post by ID
     * @param id ID of post to find
     * @return Post
     * @throws ResourceNotFoundException if post is not found
     */
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    /**
     * Update a post
     * @param id ID of post to update
     * @param postRequest Post update request
     * @param userId ID of user updating the post
     * @return Updated post
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public Post updatePost(Long id, PostRequest postRequest, Long userId) {
        Post post = findById(id);

        // Check if current user is the post owner or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to update this post");
        }

        if (postRequest.getContent() != null) {
            post.setContent(postRequest.getContent());
        }

        if (postRequest.getFileUrl() != null) {
            post.setFileUrl(postRequest.getFileUrl());
        }

        if (postRequest.getFileType() != null) {
            post.setFileType(postRequest.getFileType());
        }

        return postRepository.save(post);
    }

    /**
     * Delete a post
     * @param id ID of post to delete
     * @param userId ID of user deleting the post
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public void deletePost(Long id, Long userId) {
        Post post = findById(id);

        // Check if current user is the post owner or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this post");
        }

        postRepository.delete(post);
    }

     /**
     * Admin Delete a post
     * @param id ID of post to delete
     * @param userId ID of user deleting the post
     */
    @Transactional
    @CacheEvict(value = {"posts", "postStats"}, allEntries = true)
    public void deletePost(Long id) {
        Post post = findById(id);

        postRepository.delete(post);
    }

    /**
     * Get posts by user
     * @param userId ID of user to get posts for
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @Cacheable(value = "posts", key = "'user_' + #userId + '_search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getPostsByUser(Long userId, String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return postRepository.searchUserPosts(userId, searchTerm, pageable);
        }
        return postRepository.findByUserId(userId, pageable);
    }

    /**
     * Get posts by group
     * @param groupId ID of group to get posts for
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @Cacheable(value = "posts", key = "'group_' + #groupId + '_search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getPostsByGroup(Long groupId, String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return postRepository.searchGroupPosts(groupId, searchTerm, pageable);
        }
        return postRepository.findByGroupId(groupId, pageable);
    }

    /**
     * Get feed for current user
     * @param userId ID of current user
     * @param pageable Pagination parameters
     * @return Page of posts visible to current user
     */
    public Page<Post> getFeed(Long userId, Pageable pageable) {
        return postRepository.findVisiblePosts(userId, pageable); 
    }

    /**
     * Admin only: Get feed for all users
     * @param pageable Pagination parameters
     * @return Page of posts visible to current user
     * findAll is not for user, since user visible Posts are always specific to current user, 
     * hence for Users use findVisiblePosts instead of findAll
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable); 
    }

    /**
     * Get posts ordered by engagement (likes and comments)
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @Cacheable(value = "postStats", key = "'engagement_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getPostsOrderedByEngagement(Pageable pageable) {
        return postRepository.findPostsOrderedByEngagement(pageable);
    }

    /**
     * Get statistics about posts grouped by creation date
     * @return List of objects containing date and count
     */
    @Cacheable(value = "postStats", key = "'byDate'")
    public List<Object[]> getPostStatsByDate() {
        return postRepository.countPostsByCreationDate();
    }

    /**
     * Get statistics about posts grouped by user
     * @return List of objects containing user ID and count
     */
    @Cacheable(value = "postStats", key = "'byUser'")
    public List<Object[]> getPostStatsByUser() {
        return postRepository.countPostsByUser();
    }

    /**
     * Get statistics about posts grouped by file type
     * @return List of objects containing file type and count
     */
    @Cacheable(value = "postStats", key = "'byFileType'")
    public List<Object[]> getPostStatsByFileType() {
        return postRepository.countPostsByFileType();
    }
}
