package controllers;

import static entities.ErrorResponse.NO_POST_MESSAGE;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import SpringContainer.annotations.beans.*;
import entities.*;
import exceptions.NotFoundException;
import mappers.*;

import java.util.List;

@RestController
public class PostControllerImpl implements PostController {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;


    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    public Object getPostById(Integer id) {
        Post post = postMapper.getPostById(id);
        if (post == null)
            throwPostException(id, "");

        return post;
    }

    public List<Comment> getCommentsByPostId(Integer id) {
        return commentMapper.getCommentsByPostId(id);
    }

    public Object createPost(Post post) {
        postMapper.insertPost(post);
        return post;
    }

    public Object updatePost(Post post, Integer id) {
        post.postId = id;

        int affectedRows = postMapper.updatePost(post);
        if (affectedRows != 1)
            throwPostException(id, " was updated");

        return post;
    }

    public Object deletePost(Integer id) {
        int affectedRows = postMapper.deletePost(id);
        if (affectedRows != 1)
            throwPostException(id, " was deleted");
        return id;
    }

    private void throwPostException(Integer id, String extraText) {
        ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND, NO_POST_MESSAGE + id + extraText);
        throw new NotFoundException(errorResponse.toString());
    }
}
