package com.hashedin.huSpark.service;

import com.hashedin.huSpark.dto.ShareRequest;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Post sharePost(Long postId, Long userId) {
        return sharePost(postId, userId, new ShareRequest());
    }

    @Transactional
    public Post sharePost(Long postId, Long userId, ShareRequest shareRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));


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

        return postRepository.save(sharedPost);
    }
}