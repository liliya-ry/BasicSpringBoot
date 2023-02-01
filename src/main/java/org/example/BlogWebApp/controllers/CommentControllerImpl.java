package org.example.BlogWebApp.controllers;

import org.example.BlogWebApp.entities.Comment;
import org.example.BlogWebApp.mappers.CommentMapper;
import org.example.SpringContainer.annotations.beans.*;

import java.util.List;

@RestController
public class CommentControllerImpl implements CommentController {
    @Autowired
    CommentMapper commentMapper;

    public List<Comment> getAllCommentsByPostId(Integer postId) {
        return commentMapper.getCommentsByPostId(postId);
    }
}
