package com.hashedin.huSpark.controller;

import java.util.List; // Import CommentDto
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.dto.CommentDto;
import com.hashedin.huSpark.dto.CommentRequestDto;
import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.InteractionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;


/**
 * Controller for interaction operations (likes, comments).
 * Handles liking/unliking posts and adding/retrieving comments.
 */
@RestController
@RequestMapping("/api/interactions")
@Api(tags = "Interactions")
public class InteractionController {

    private final Logger log = LoggerFactory.getLogger(InteractionController.class);
    private final InteractionService interactionService;
    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Constructor for InteractionController.
     * @param interactionService Service for interaction operations.
     */
    @Autowired
    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    /**
     * Likes a post.
     * @param postId Post ID.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing a success message or an error response.
     */
    @PostMapping("/like/{postId}")
    @ApiOperation("Like a post")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("InteractionController: like: UserId: " + currentUser.getId());

        try {
            boolean liked = interactionService.likePost(postId, currentUser.getId());
            return ResponseEntity.ok(liked ? "Post liked" : "Post already liked");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to like post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to like post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Unlikes a post.
     * @param postId Post ID.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing a success message or an error response.
     */
    @DeleteMapping("/like/{postId}")
    @ApiOperation("Unlike a post")
    public ResponseEntity<?> unlikePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("InteractionController: unLike: UserId: " + currentUser.getId());

        try {
            boolean unliked = interactionService.unlikePost(postId, currentUser.getId());
            return ResponseEntity.ok(unliked ? "Post unliked" : "Post was not liked");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to unlike post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to unlike post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Adds a comment to a post.
     * @param postId Post ID.
     * @param content Comment content.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the created CommentDto or an error response.
     */
    @PostMapping("/comment/{postId}")
    @ApiOperation("Add a comment to a post")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @CurrentUser UserPrincipal currentUser) {
        log.info("InteractionController: comment: UserId: " + currentUser.getId());

        try {
            Comment comment = interactionService.addComment(postId, currentUser.getId(), commentRequestDto);
            return ResponseEntity.ok(modelMapper.map(comment, CommentDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Failed to add comment: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves comments for a post.
     * @param postId Post ID.
     * @return ResponseEntity containing a list of CommentDto or an error response.
     */
    @GetMapping("/comments/{postId}")
    @ApiOperation("Get comments for a post")
    public ResponseEntity<List<CommentDto>> getCommentsByPost(@PathVariable Long postId) {
        log.info("InteractionController: getComments: PostId: " + postId);

        try {
            List<Comment> comments = interactionService.getCommentsByPost(postId);
            List<CommentDto> commentDtos = comments.stream()
                    .map(comment -> modelMapper.map(comment, CommentDto.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(commentDtos);
        } catch (Exception e) {
            log.error("Failed to get comments for post: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}