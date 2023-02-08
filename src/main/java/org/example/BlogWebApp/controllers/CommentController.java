package org.example.BlogWebApp.controllers;

import org.example.BlogWebApp.entities.Comment;
import org.example.SpringFramework.SpringContainer.annotations.web.GetMapping;
import org.example.SpringFramework.SpringContainer.annotations.web.RequestMapping;
import org.example.SpringFramework.SpringContainer.annotations.web.RequestParam;

import java.util.List;

@RequestMapping("/comments")
public interface CommentController {
    @GetMapping
    List<Comment> getAllCommentsByPostId(@RequestParam Integer postId);
}
