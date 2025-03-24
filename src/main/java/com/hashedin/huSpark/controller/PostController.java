package com.hashedin.huSpark.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.dto.PostCreationDateDTO;
import com.hashedin.huSpark.dto.PostDto;
import com.hashedin.huSpark.dto.PostFileTypeCountDTO;
import com.hashedin.huSpark.dto.PostRequest;
import com.hashedin.huSpark.dto.ShareRequest;
import com.hashedin.huSpark.dto.UserPostCountDTO;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.PostService;
import com.hashedin.huSpark.service.ShareService;

import io.swagger.annotations.Api; // Import PostDto
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

/**
 * Controller for post operations.
 * Handles creating, sharing, retrieving, updating, and deleting posts.
 */
@RestController
@RequestMapping("/api/posts")
@Api(tags = "Posts")
public class PostController {

    private final Logger log = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final ShareService shareService;
    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Constructor for PostController.
     * @param postService Service for post operations.
     * @param shareService Service for sharing posts.
     */
    @Autowired
    public PostController(PostService postService, ShareService shareService) {
        this.postService = postService;
        this.shareService = shareService;
    }

    /**
     * Creates a new post.
     * @param postRequest Post creation request.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the created PostDto or an error response.
     */
    @PostMapping
    @ApiOperation("Create a new post")
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody PostRequest postRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: createPost: UserId: " + currentUser.getId());

        try {
            Post post = postService.createPost(postRequest, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(post, PostDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Failed to create post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Shares a post.
     * @param postId Post ID to share.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the shared PostDto or an error response.
     */
    @PostMapping("/{postId}/share")
    @ApiOperation("Share a post")
    public ResponseEntity<PostDto> sharePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: createPost: UserId: " + currentUser.getId());

        try {
            Post post = shareService.sharePost(postId, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(post, PostDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to share post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Failed to share post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Shares a post with advanced options.
     * @param postId ID of the post to share.
     * @param shareRequest Request with sharing options.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the shared PostDto or an error response.
     */
    @PostMapping("/{postId}/share/advanced")
    @ApiOperation(value = "Share another user's post with advanced options",
            notes = "Creates a customized share based on the provided options")
    public ResponseEntity<PostDto> sharePostAdvanced(
            @PathVariable Long postId,
            @RequestBody ShareRequest shareRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: sharePostAdvanced: UserId: " + currentUser.getId());

        try {
            Post sharedPost = shareService.sharePost(postId, currentUser.getId(), shareRequest);
            return ResponseEntity.ok(modelMapper.map(sharedPost, PostDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to share post with advanced options: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Failed to share post with advanced options: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a post by its ID.
     * @param id Post ID.
     * @return ResponseEntity containing the PostDto or an error response.
     */
    @GetMapping("/{id}")
    @ApiOperation("Get a post by ID")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        log.info("PostController: getPost: postID: " + id);

        try {
            Post post = postService.findById(id);
            return ResponseEntity.ok(modelMapper.map(post, PostDto.class));
        } catch (Exception e) {
            log.error("Failed to get post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates a post.
     * @param id Post ID.
     * @param postRequest Post update request.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the updated PostDto or an error response.
     */
    @PutMapping("/{id}")
    @ApiOperation("Update a post")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest postRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("PostController: UpdatePost: UserId: " + currentUser.getId());

        try {
            Post updatedPost = postService.updatePost(id, postRequest, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(updatedPost, PostDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Failed to update post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Admin: Deletes a post.
     * @param id Post ID.
     * @return ResponseEntity indicating successful deletion or an error response.
     */
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Delete a post")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete post (admin): " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        try {
            postService.deletePost(id, currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete post (user): " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get posts by user.
     * @param userId User ID.
     * @param searchTerm Search term for filtering.
     * @param pageable Pagination parameters.
     * @return Page of PostDto.
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("Get posts by user")
    public ResponseEntity<Page<PostDto>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        try{
            Page<Post> posts = postService.getPostsByUser(userId, searchTerm, pageable);
            Page<PostDto> postDtos = posts.map(post -> modelMapper.map(post, PostDto.class));
            return ResponseEntity.ok(postDtos);
        }catch (Exception e){
            log.error("Failed to get posts by user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    /**
     * Get posts by group.
     * @param groupId Group ID.
     * @param searchTerm Search term for filtering.
     * @param pageable Pagination parameters.
     * @return Page of PostDto.
     */
    @GetMapping("/group/{groupId}")
    @ApiOperation("Get posts by group")
    public ResponseEntity<Page<PostDto>> getPostsByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {

        try{
            Page<Post> posts = postService.getPostsByGroup(groupId, searchTerm, pageable);
            Page<PostDto> postDtos = posts.map(post -> modelMapper.map(post, PostDto.class));
            return ResponseEntity.ok(postDtos);
        }catch (Exception e){
            log.error("Failed to get posts by group: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    /**
     * Get feed for current user.
     * @param currentUser Current authenticated user.
     * @param pageable Pagination parameters.
     * @return Page of PostDto.
     */
    @GetMapping("/feed")
    @ApiOperation("Get feed for current user")
    public ResponseEntity<Page<PostDto>> getFeed(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        try{
            Page<Post> posts = postService.getFeed(currentUser.getId(), pageable);
            Page<PostDto> postDtos = posts.map(post -> modelMapper.map(post, PostDto.class));
            return ResponseEntity.ok(postDtos);
        }catch (Exception e){
            log.error("Failed to get feed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

     /**
     * Admin: Get All Posts 
     * @param pageable Pagination parameters.
     * @return Page of PostDto.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("Get All Posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            Pageable pageable) {
        try{
            Page<Post> posts = postService.getAllPosts(pageable);
            Page<PostDto> postDtos = posts.map(post -> modelMapper.map(post, PostDto.class));
            return ResponseEntity.ok(postDtos);
        }catch (Exception e){
            log.error("Failed to get all posts: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    /**
     * Get posts ordered by engagement.
     * @param pageable Pagination parameters.
     * @return Page of PostDto.
     */
    @GetMapping("/stats/engagement")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get posts ordered by engagement (Admin only)")
    public ResponseEntity<Page<PostDto>> getPostsOrderedByEngagement(Pageable pageable) {
        try{
            Page<Post> posts = postService.getPostsOrderedByEngagement(pageable);
            Page<PostDto> postDtos = posts.map(post -> modelMapper.map(post, PostDto.class));
            return ResponseEntity.ok(postDtos);
        }catch (Exception e){
            log.error("Failed to get posts ordered by engagement: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    /**
     * Get post statistics by date.
     * @return List of objects containing date and count.
     */
    @GetMapping("/stats/by-date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get post statistics by date (Admin only)")
    public ResponseEntity<List<PostCreationDateDTO>> getPostStatsByDate() {
        try {
            List<PostCreationDateDTO> stats = postService.getPostStatsByDate();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get post statistics by date: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get post statistics by user.
     * @return List of objects containing user ID and count.
     */
    @GetMapping("/stats/by-user")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get post statistics by user (Admin only)")
    public ResponseEntity<List<UserPostCountDTO>> getPostStatsByUser() {
        try {
            List<UserPostCountDTO> stats = postService.getPostStatsByUser();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get post statistics by user: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get post statistics by file type.
     * @return List of objects containing file type and count.
     */
    @GetMapping("/stats/by-file-type")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get post statistics by file type (Admin only)")
    public ResponseEntity<List<PostFileTypeCountDTO>> getPostStatsByFileType() {
        log.info("PostController: getPostStatsByFileType");
        try {
            List<PostFileTypeCountDTO> stats = postService.getPostStatsByFileType();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get post statistics by file type: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
