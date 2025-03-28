package com.hashedin.huSpark.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.PostCreationDateDTO;
import com.hashedin.huSpark.dto.PostFileTypeCountDTO;
import com.hashedin.huSpark.dto.PostRequest;
import com.hashedin.huSpark.dto.UserPostCountDTO;
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
 * Service for post-related operations.
 */
@Service
public class PostService {

    private final Logger log = LoggerFactory.getLogger(PostService.class);

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

    @Transactional
    public Group getGroupWithDetails(Long groupId) {
        Group group = groupRepository.findByIdWithoutRelations(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        groupRepository.findGroupWithMembers(groupId).ifPresent(g -> Hibernate.initialize(g.getMembers()));
        groupRepository.findGroupWithPosts(groupId).ifPresent(g -> Hibernate.initialize(g.getPosts()));

        return group;
    }

    /**
     * Create a new post.
     * 
     * @param postRequest Post creation request
     * @param userId      ID of user creating the post
     * @return Created post
     */
    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public Post createPost(PostRequest postRequest, Long userId) {
        log.info("PostService: createPost: UserId: {}", userId);

        User user = userService.findById(userId);
        Long groupId = postRequest.getGroupId();

        // potential circular dependency
        // User →(is part of) Group →(has) Group Members → User →(creates) Post
        // →(references) Group
        // If group ID is provided, check if user is a member of the group
        Group group = groupRepository
                .findByIdWithoutRelations/* findById *//* findByIdWithMembers *//* findGroupWithMembers */(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found with id: {}", groupId);
                    return new ResourceNotFoundException("Group not found with id: " + groupId);
                });
        // Check if user is a member of the group
        // synchronization is no longer needed since group.getMembers() is now a
        // CopyOnWriteArraySet.
        // synchronized (group.getMembers()) { // Synchronize on the Array Set
        // Clone the set to avoid modification issues
        var users = group.getMembers();
        if (!users.isEmpty()) {
            Set<User> membersCopy = new HashSet<>(users);
            if (!membersCopy.contains(user)) {
                if (!group.getCreator().getId().equals(userId)) {
                    log.warn("User {} is not a member of group {}.", userId, groupId);
                    throw new UnauthorizedException("You must be a member of the group to post");
                }
            }
        }

        Post post = Post.builder()
                .content(postRequest.getContent())
                .fileUrl(postRequest.getFileUrl())
                .fileType(postRequest.getFileType())
                .user(user)
                .isShared(false)
                .likes(0)
                .build();

        log.info("PostService: createPost: UserId: {} Post Content: {}", post.getUser().getId(), post.getContent());

        // If group ID is provided, check if user is a member of the group
        post.setGroup(group);
        log.info("PostService: createPost: Group Id: {}", post.getGroup().getId());

        Post savedPost = postRepository.saveAndFlush(post); // Changed from save to saveAndFlush
        log.info("Post created successfully: PostId: {}", savedPost.getId());
        return savedPost;
    }

    /**
     * Share an existing post.
     * 
     * @param postId ID of post to share
     * @param userId ID of user sharing the post
     * @return Shared post
     */
    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public Post sharePost(Long postId, Long userId) {
        log.info("PostService: sharePost: PostId: {}, UserId: {}", postId, userId);
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

        Post savedSharedPost = postRepository.saveAndFlush(sharedPost); // Changed from save to saveAndFlush
        log.info("Post {} shared by user {}: SharedPostId: {}", postId, userId, savedSharedPost.getId());
        return savedSharedPost;
    }

    /**
     * Find a post by ID.
     * 
     * @param id ID of post to find
     * @return Post
     * @throws ResourceNotFoundException if post is not found
     */
    public Post findById(Long id) {
        log.info("PostService: findById: PostId: {}", id);
        return postRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", id);
                    return new ResourceNotFoundException("Post not found with id: " + id);
                });
    }

    /**
     * Update a post.
     * 
     * @param id          ID of post to update
     * @param postRequest Post update request
     * @param userId      ID of user updating the post
     * @return Updated post
     */
    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public Post updatePost(Long id, PostRequest postRequest, Long userId) {
        log.info("PostService: updatePost: PostId: {}, UserId: {}", id, userId);
        Post post = findById(id);

        // Check if current user is the post owner or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !post.getUser().getId().equals(userId)) {
            log.warn("User {} does not have permission to update post {}.", userId, id);
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

        Post updatedPost = postRepository.saveAndFlush(post); // Changed from save to saveAndFlush
        log.info("Post updated successfully: PostId: {}", updatedPost.getId());
        return updatedPost;
    }

    /**
     * Delete a post.
     * 
     * @param id     ID of post to delete
     * @param userId ID of user deleting the post
     */
    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public void deletePost(Long id, Long userId) {
        log.info("PostService: deletePost: PostId: {}, UserId: {}", id, userId);
        Post post = findById(id);

        // Check if current user is the post owner or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !post.getUser().getId().equals(userId)) {
            log.warn("User {} does not have permission to delete post {}.", userId, id);
            throw new UnauthorizedException("You do not have permission to delete this post");
        }

        postRepository.delete(post);
        log.info("Post deleted successfully: PostId: {}", id);
    }

    /**
     * Get posts by user
     * 
     * @param userId     ID of user to get posts for
     * @param searchTerm Search term for filtering
     * @param pageable   Pagination parameters
     * @return Page of posts
     */
    @Cacheable(value = "posts", key = "'user_' + #userId + '_search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getPostsByUser(Long userId, String searchTerm, Pageable pageable) {
        log.info("PostService: getPostsByUser: UserId: {}, SearchTerm: {}, Pageable: {}", userId, searchTerm, pageable);
        Page<Post> posts;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            posts = postRepository.searchUserPosts(userId, searchTerm, pageable);
        } else {
            posts = postRepository.findByUserId(userId, pageable);
        }
        log.info("Found {} posts for user {}.", posts.getTotalElements(), userId);
        return posts;
    }

    /**
     * Get posts by group
     * 
     * @param groupId    ID of group to get posts for
     * @param searchTerm Search term for filtering
     * @param pageable   Pagination parameters
     * @return Page of posts
     */
    @Cacheable(value = "posts", key = "'group_' + #groupId + '_search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getPostsByGroup(Long groupId, String searchTerm, Pageable pageable) {
        log.info("PostService: getPostsByGroup: GroupId: {}, SearchTerm: {}, Pageable: {}", groupId, searchTerm,
                pageable);
        Page<Post> posts;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            posts = postRepository.searchGroupPosts(groupId, searchTerm, pageable);
        } else {
            posts = postRepository.findByGroupId(groupId, pageable);
        }
        log.info("Found {} posts for group {}.", posts.getTotalElements(), groupId);
        return posts;
    }

    /**
     * Get feed for current user
     * 
     * @param userId   ID of current user
     * @param pageable Pagination parameters
     * @return Page of posts visible to current user
     */
    public Page<Post> getFeed(Long userId, Pageable pageable) {
        log.info("PostService: getFeed: UserId: {}, Pageable: {}", userId, pageable);
        Page<Post> feed = postRepository.findVisiblePosts(userId, pageable);
        log.info("Found {} feed posts for user {}.", feed.getTotalElements(), userId);
        return feed;
    }

    /**
     * Admin only: Get feed for all users
     * 
     * @param pageable Pagination parameters
     * @currentUserId current authenthicated loggedin User
     * @return Page of posts visible to current user
     *         findAll is not for user, since user visible Posts are always specific
     *         to current user,
     *         hence for Users use findVisiblePosts instead of findAll
     */
    @Transactional(readOnly = true) // 🔹 Ensures Hibernate session remains open
    public Page<Post> getAllPosts(Pageable pageable, Long currentUserId) {
        log.info("PostService: getAllPosts: Pageable: {}", pageable);
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin()) {
            log.warn("Unauthorized access: User {} does not have permission to getAllPosts.", currentUserId);
            throw new UnauthorizedException("Unauthorized access: You do not have permission to getAllPosts");
        }
        Page<Post> allPosts = postRepository.findAll(pageable);
        // Force loading of lazy collections
        allPosts.forEach(post -> {
            try {
                Hibernate.initialize(post.getComments());
            } catch (Exception e) {
            }
            try {
                Hibernate.initialize(post.getPostLikes());
            } catch (Exception e) {
            }
        });
        log.info("Found {} all posts.", allPosts.getTotalElements());
        return allPosts;
    }

    /**
     * Get posts ordered by engagement (likes and comments)
     * 
     * @param pageable      Pagination parameters
     * @param currentUserId loginuser parameter
     * @return Page of posts
     */
    @Cacheable(value = "postStats", key = "'engagement_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getPostsOrderedByEngagement(Pageable pageable, Long currentUserId) {
        log.info("PostService: getPostsOrderedByEngagement: Pageable: {}", pageable);
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin()) {
            log.warn("Unauthorized access: User {} does not have permission to getPostsOrderedByEngagement.",
                    currentUserId);
            throw new UnauthorizedException(
                    "Unauthorized access: You do not have permission to getPostsOrderedByEngagement");
        }
        Page<Post> orderedPosts = postRepository.findPostsOrderedByEngagement(pageable);
        log.info("Found {} posts ordered by engagement.", orderedPosts.getTotalElements());
        return orderedPosts;
    }

    /**
     * Get statistics about posts grouped by creation date
     * 
     * @currentUserId loginUser parameter
     * @return List of objects containing date and count
     */
    @Cacheable(value = "postStats", key = "'byDate'")
    public List<PostCreationDateDTO> getPostStatsByDate(Long currentUserId) {
        log.info("PostService: getPostStatsByDate");
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin()) {
            log.warn("Unauthorized access: User {} does not have permission to getPostStatsByDate.", currentUserId);
            throw new UnauthorizedException("Unauthorized access: You do not have permission to getPostStatsByDate");
        }
        List<PostCreationDateDTO> stats = postRepository.countPostsByCreationDate();
        log.info("Found {} post stats by date.", stats.size());
        return stats;
    }

    /**
     * Get statistics about posts grouped by user
     * 
     * @param currentUserId loginUser parameter
     * @return List of objects containing user ID and count
     */
    @Cacheable(value = "postStats", key = "'byUser'")
    public List<UserPostCountDTO> getPostStatsByUser(Long currentUserId) {
        log.info("PostService: getPostStatsByUser");

        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin()) {
            log.warn("Unauthorized access: User {} does not have permission to getPostStatsByUser.", currentUserId);
            throw new UnauthorizedException("Unauthorized access: You do not have permission to getPostStatsByUser");
        }

        List<UserPostCountDTO> stats = postRepository.countPostsByUser();
        log.info("Found {} post stats by user.", stats.size());
        return stats;
    }

    /**
     * Get statistics about posts grouped by file type
     * 
     * @currentUserId loginUser @parameter
     * @return List of objects containing file type and count
     */
    @Cacheable(value = "postStats", key = "'byFileType'")
    public List<PostFileTypeCountDTO> getPostStatsByFileType(Long currentUserId) {
        log.info("PostService: getPostStatsByFileType");

        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin()) {
            log.warn("Unauthorized access: User {} does not have permission to getPostStatsByFileType.", currentUserId);
            throw new UnauthorizedException(
                    "Unauthorized access: You do not have permission to getPostStatsByFileType");
        }

        List<PostFileTypeCountDTO> stats = postRepository.countPostsByFileType();
        log.info("Found {} post stats by file type.", stats.size());
        return stats;
    }
}
