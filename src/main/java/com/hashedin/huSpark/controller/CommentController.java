package com.hashedin.huSpark.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.service.CommentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller for comment-related operations.
 * Handles adding comments and retrieving comments for a specific post.
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "Comments")
public class CommentController {

    private final Logger log = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    /**
     * Constructor for CommentController.
     * @param commentService Service for comment operations.
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Adds a new comment.
     * @param request A map containing userId, postId, and content.
     * @return ResponseEntity containing the added comment or an error response.
     */
    @PostMapping("/add")
    @ApiOperation("Add a new comment")
    public ResponseEntity<Comment> addComment(@RequestBody Map<String, String> request) {
        log.info("CommentController: add");

        try {
            Long userId = Long.parseLong(request.get("userId"));
            Long postId = Long.parseLong(request.get("postId"));
            String content = request.get("content");
            Comment comment = commentService.addComment(userId, postId, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment); // 201 Created
        } catch (NumberFormatException e) {
            log.warn("Invalid number format in comment request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid comment request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Failed to add comment: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves comments for a specific post.
     * @param postId ID of the post to retrieve comments for.
     * @return ResponseEntity containing a list of comments or an error response.
     */
    @GetMapping("/post/{postId}")
    @ApiOperation("Get comments by post ID")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        log.info("CommentController: post");

        try {
            List<Comment> comments = commentService.getCommentsByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Failed to get comments for post " + postId + ": " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}