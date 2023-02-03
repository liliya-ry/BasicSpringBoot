package org.example.BlogWebApp.controllers;

import org.example.BlogWebApp.entities.Comment;
import org.example.SpringContainer.annotations.web.*;

import java.util.List;

@RequestMapping("/comments")
public interface CommentController {
    @GetMapping
    List<Comment> getAllCommentsByPostId(@RequestParam Integer postId);
}
