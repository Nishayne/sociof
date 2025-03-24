package com.hashedin.huSpark.controller;

import org.modelmapper.ModelMapper; // Import PostDto
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.dto.PostDto;
import com.hashedin.huSpark.dto.ShareRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.ShareService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller for post sharing functionality.
 * Handles sharing posts with basic and advanced options.
 */
@RestController
@RequestMapping("/api/shares")
@Api(tags = "Post Sharing")
public class ShareController {

    private final Logger log = LoggerFactory.getLogger(ShareController.class);
    private final ShareService shareService;
    private final ModelMapper modelMapper = new ModelMapper();

    /**
     * Constructor for ShareController.
     * @param shareService Service for sharing posts.
     */
    @Autowired
    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    /**
     * Shares a post with basic options.
     * @param postId ID of the post to share.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing the shared PostDto or an error response.
     */
    @PostMapping("/{postId}")
    @ApiOperation(value = "Share another user's post",
            notes = "Creates a duplicate of the original post with reference URLs to the original user and post")
    public ResponseEntity<PostDto> sharePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("ShareController: sharePost: UserId: " + currentUser.getId());
        try {
            Post sharedPost = shareService.sharePost(postId, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(sharedPost, PostDto.class));
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
    @PostMapping("/{postId}/advanced")
    @ApiOperation(value = "Share another user's post with advanced options",
            notes = "Creates a customized share based on the provided options")
    public ResponseEntity<PostDto> sharePostAdvanced(
            @PathVariable Long postId,
            @RequestBody ShareRequest shareRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("ShareController: sharePostAdvanced: UserId: " + currentUser.getId());
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
}