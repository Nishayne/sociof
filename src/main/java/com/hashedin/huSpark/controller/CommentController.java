package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.service.CommentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;
    public CommentController(CommentService commentService){
        this.commentService=commentService;
    }

    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody Map<String, String> request){
        log.info("CommentController: add");

        Long userId= Long.parseLong(request.get("userId"));
        Long postId= Long.parseLong(request.get("postId"));
        String content =request.get("content");
        Comment comment=commentService.addComment(userId, postId, content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId){
        log.info("CommentController: post");

        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}
