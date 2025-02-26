package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.InteractionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for interaction operations (likes, comments)
 */
@RestController
@RequestMapping("/api/interactions")
@Api(tags = "Interactions")
public class InteractionController {

    Logger log = LoggerFactory.getLogger(InteractionController.class);

    @Autowired
    private InteractionService interactionService;

    /**
     * Like a post
     * @param postId Post ID
     * @param currentUser Current authenticated user
     * @return Success message
     */
    @PostMapping("/like/{postId}")
    @ApiOperation("Like a post")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("InteractionController: like: UserId: " + currentUser.getId());

        boolean liked = interactionService.likePost(postId, currentUser.getId());
        return ResponseEntity.ok(liked ? "Post liked" : "Post already liked");
    }

    /**
     * Unlike a post
     * @param postId Post ID
     * @param currentUser Current authenticated user
     * @return Success message
     */
    @DeleteMapping("/like/{postId}")
    @ApiOperation("Unlike a post")
    public ResponseEntity<?> unlikePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("InteractionController: unLike: UserId: " + currentUser.getId());

        boolean unliked = interactionService.unlikePost(postId, currentUser.getId());
        return ResponseEntity.ok(unliked ? "Post unliked" : "Post was not liked");
    }

    /**
     * Add a comment to a post
     * @param postId Post ID
     * @param content Comment content
     * @param currentUser Current authenticated user
     * @return Created comment
     */
    @PostMapping("/comment/{postId}")
    @ApiOperation("Add a comment to a post")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody @NotBlank String content,
            @CurrentUser UserPrincipal currentUser) {
        log.info("InteractionController: comment: UserId: " + currentUser.getId());

        Comment comment = interactionService.addComment(postId, currentUser.getId(), content);
        return ResponseEntity.ok(comment);
    }

    /**
     * Get comments for a post
     * @param postId Post ID
     * @return List of comments
     */
    @GetMapping("/comments/{postId}")
    @ApiOperation("Get comments for a post")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        log.info("InteractionController: getComments: PostId: " + postId);

        List<Comment> comments = interactionService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }
}
