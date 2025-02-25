package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.dto.ShareRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.ShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for post sharing functionality
 */
@RestController
@RequestMapping("/api/shares")
@Api(tags = "Post Sharing")
public class ShareController {

    @Autowired
    private ShareService shareService;

    /**
     * Share a post with basic options
     *
     * @param postId ID of the post to share
     * @param currentUser Current authenticated user
     * @return Shared post object
     */
    @PostMapping("/{postId}")
    @ApiOperation(value = "Share another user's post",
            notes = "Creates a duplicate of the original post with reference URLs to the original user and post")
    public ResponseEntity<Post> sharePost(
            @PathVariable Long postId,
            @CurrentUser UserPrincipal currentUser) {

        Post sharedPost = shareService.sharePost(postId, currentUser.getId());
        return ResponseEntity.ok(sharedPost);
    }

    /**
     * Share a post with advanced options
     *
     * @param postId ID of the post to share
     * @param shareRequest Request with sharing options
     * @param currentUser Current authenticated user
     * @return Shared post object
     */
    @PostMapping("/{postId}/advanced")
    @ApiOperation(value = "Share another user's post with advanced options",
            notes = "Creates a customized share based on the provided options")
    public ResponseEntity<Post> sharePostAdvanced(
            @PathVariable Long postId,
            @RequestBody ShareRequest shareRequest,
            @CurrentUser UserPrincipal currentUser) {

        Post sharedPost = shareService.sharePost(postId, currentUser.getId(), shareRequest);
        return ResponseEntity.ok(sharedPost);
    }
}
