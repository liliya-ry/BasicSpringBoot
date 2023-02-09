package controllers;

import SpringContainer.annotations.web.*;
import entities.Comment;

import java.util.List;

@RequestMapping("/comments")
public interface CommentController {
    @GetMapping
    List<Comment> getAllCommentsByPostId(@RequestParam Integer postId);
}
