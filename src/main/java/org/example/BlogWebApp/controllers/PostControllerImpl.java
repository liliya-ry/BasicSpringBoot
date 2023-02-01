package org.example.BlogWebApp.controllers;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.example.BlogWebApp.entities.ErrorResponse.NO_POST_MESSAGE;

import org.example.BlogWebApp.entities.*;
import org.example.BlogWebApp.exceptions.NotFoundException;
import org.example.BlogWebApp.mappers.*;
import org.example.SpringContainer.annotations.beans.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@RestController
public class PostControllerImpl implements PostController {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ObjectMapper objectMapper;

    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    public Object getPostById(Integer id) throws JsonProcessingException {
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

    public Object updatePost(Post post, Integer id) throws JsonProcessingException {
        post.id = id;

        int affectedRows = postMapper.updatePost(post);
        if (affectedRows != 1)
            throwPostException(id, " was updated");

        return post;
    }

    public Object deletePost(Integer id) throws JsonProcessingException {
        int affectedRows = postMapper.deletePost(id);
        if (affectedRows != 1)
            throwPostException(id, " was deleted");
        return id;
    }

    private void throwPostException(Integer id, String extraText) throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND, NO_POST_MESSAGE + id + extraText);
        String jsonError = objectMapper.writeValueAsString(errorResponse);
        throw new NotFoundException(jsonError);
    }
}
