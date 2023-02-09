package controllers;

import entities.Comment;
import mappers.CommentMapper;
import SpringContainer.annotations.beans.*;
import java.util.List;

@RestController
public class CommentControllerImpl implements CommentController {
    @Autowired
    CommentMapper commentMapper;

    public List<Comment> getAllCommentsByPostId(Integer postId) {
        return commentMapper.getCommentsByPostId(postId);
    }
}
