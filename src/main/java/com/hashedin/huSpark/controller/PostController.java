package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.dto.PostDto;
import com.hashedin.huSpark.dto.PostRequest;
import com.hashedin.huSpark.dto.ShareRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.PostService;
import com.hashedin.huSpark.service.ShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for post operations
 */
@RestController
@RequestMapping("/api/posts")
@Api(tags = "Posts")
public class PostController {

    Logger log = LoggerFactory.getLogger(PostController.class);


    @Autowired
    private PostService postService;

    @Autowired
    private ShareService shareService;

    /**
     * Create a new post
     * @param postRequest Post creation request
     * @param currentUser Current authenticated user
     * @return Created post
     */
    @PostMapping
    @ApiOperation("Create a new post")
    public ResponseEntity<Post> createPost(
            @Valid @RequestBody PostRequest postRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: createPost: UserId: " + currentUser.getId());

        Post post = postService.createPost(postRequest, currentUser.getId());
        return ResponseEntity.ok(post);
    }

    /**
     * Share a post
     * @param postId Post ID to share
     * @param currentUser Current authenticated user
     * @return Shared post
     */
    @PostMapping("/{postId}/share")
    @ApiOperation("Share a post")
    public ResponseEntity<Post> sharePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: createPost: UserId: " + currentUser.getId());

        Post post = shareService.sharePost(postId, currentUser.getId());
        return ResponseEntity.ok(post);
    }

    /**
     * Share a post with advanced options
     * @param postId ID of the post to share
     * @param shareRequest Request with sharing options
     * @param currentUser Current authenticated user
     * @return Shared post object
     */
    @PostMapping("/{postId}/share/advanced")
    @ApiOperation(value = "Share another user's post with advanced options",
            notes = "Creates a customized share based on the provided options")
    public ResponseEntity<Post> sharePostAdvanced(
            @PathVariable Long postId,
            @RequestBody ShareRequest shareRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: sharePostAdvanced: UserId: " + currentUser.getId());

        Post sharedPost = shareService.sharePost(postId, currentUser.getId(), shareRequest);
        return ResponseEntity.ok(sharedPost);
    }

    /**
     * Get a post by ID
     * @param id Post ID
     * @return Post
     */
    @GetMapping("/{id}")
    @ApiOperation("Get a post by ID")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        log.info("PostController: getPost: postID: " + id);

        Post post = postService.findById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * Update a post
     * @param id Post ID
     * @param postRequest Post update request
     * @param currentUser Current authenticated user
     * @return Updated post
     */
    @PutMapping("/{id}")
    @ApiOperation("Update a post")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest postRequest,
            @CurrentUser UserPrincipal currentUser) {

        log.info("PostController: UpdatePost: UserId: " + currentUser.getId());

        Post updatedPost = postService.updatePost(id, postRequest, currentUser.getId());
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Delete a post
     * @param id Post ID
     * @param currentUser Current authenticated user
     * @return Deleted post
     */
    @DeleteMapping("/{id}")
    @ApiOperation("Delete a post")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        postService.deletePost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Get posts by user
     * @param userId User ID
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("Get posts by user")
    public ResponseEntity<Page<Post>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Post> posts = postService.getPostsByUser(userId, searchTerm, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get posts by group
     * @param groupId Group ID
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @GetMapping("/group/{groupId}")
    @ApiOperation("Get posts by group")
    public ResponseEntity<Page<Post>> getPostsByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Post> posts = postService.getPostsByGroup(groupId, searchTerm, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get feed for current user
     * @param currentUser Current authenticated user
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @GetMapping("/feed")
    @ApiOperation("Get feed for current user")
    public ResponseEntity<Page<Post>> getFeed(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        Page<Post> posts = postService.getFeed(currentUser.getId(), pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get posts ordered by engagement
     * @param pageable Pagination parameters
     * @return Page of posts
     */
    @GetMapping("/stats/engagement")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get posts ordered by engagement (Admin only)")
    public ResponseEntity<Page<Post>> getPostsOrderedByEngagement(Pageable pageable) {
        Page<Post> posts = postService.getPostsOrderedByEngagement(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get post statistics by date
     * @return List of objects containing date and count
     */
    @GetMapping("/stats/by-date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get post statistics by date (Admin only)")
    public ResponseEntity<List<Object[]>> getPostStatsByDate() {
        List<Object[]> stats = postService.getPostStatsByDate();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get post statistics by user
     * @return List of objects containing user ID and count
     */
    @GetMapping("/stats/by-user")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get post statistics by user (Admin only)")
    public ResponseEntity<List<Object[]>> getPostStatsByUser() {
        List<Object[]> stats = postService.getPostStatsByUser();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get post statistics by file type
     * @return List of objects containing file type and count
     */
    @GetMapping("/stats/by-file-type")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get post statistics by file type (Admin only)")
    public ResponseEntity<List<Object[]>> getPostStatsByFileType() {
        List<Object[]> stats = postService.getPostStatsByFileType();
        return ResponseEntity.ok(stats);
    }
}