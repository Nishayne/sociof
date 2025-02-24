package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    public CommentController(CommentService commentService){
        this.commentService=commentService;
    }

    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody Map<String, String> request){
        Long userId= Long.parseLong(request.get("userId"));
        Long postId= Long.parseLong(request.get("postId"));
        String content =request.get("content");
        Comment comment=commentService.addComment(userId, postId, content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId){
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}
