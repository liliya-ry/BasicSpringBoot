package org.example.BlogWebApp.controllers;

import org.example.BlogWebApp.entities.Comment;
import org.example.BlogWebApp.mappers.CommentMapper;
import org.example.SpringContainer.annotations.beans.*;
import org.example.SpringContainer.annotations.web.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentControllerImpl {
    @Autowired
    CommentMapper commentMapper;

    @GetMapping
    public List<Comment> getAllCommentsByPostId(Integer postId) {
        return commentMapper.getCommentsByPostId(postId);
    }
}
