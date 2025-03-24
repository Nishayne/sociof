package com.hashedin.huSpark.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.ShareRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.UserRepository;

@Service
public class ShareService {

    private final Logger log = LoggerFactory.getLogger(ShareService.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Post sharePost(Long postId, Long userId) {
        log.info("ShareService: sharePost: PostId: {}, UserId: {}", postId, userId);
        return sharePost(postId, userId, new ShareRequest());
    }

    @Transactional
    public Post sharePost(Long postId, Long userId, ShareRequest shareRequest) {
        log.info("ShareService: sharePost: PostId: {}, UserId: {}, ShareRequest: {}", postId, userId, shareRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", postId);
                    return new ResourceNotFoundException("Post not found with id: " + postId);
                });

        // Create a new post for the shared content
        Post sharedPost = Post.builder()
                .content(shareRequest.getCustomMessage() != null ?
                        shareRequest.getCustomMessage() + "\n\n" + originalPost.getContent() :
                        "Shared: " + originalPost.getContent())
                .fileUrl(originalPost.getFileUrl())
                .fileType(originalPost.getFileType())
                .likes(0)  //Reset likes for new shared post
                .isShared(true)
                .originalPostId(originalPost.getId())
                .originalUserId(originalPost.getUser().getId())
                .user(user) // Owner of new shared post
                .build();

        Post savedSharedPost = postRepository.saveAndFlush(sharedPost); // Changed from save to saveAndFlush
        log.info("Post shared successfully: SharedPostId: {}", savedSharedPost.getId());
        return savedSharedPost;
    }
}