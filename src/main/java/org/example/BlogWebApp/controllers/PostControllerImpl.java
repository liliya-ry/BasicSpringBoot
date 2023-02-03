package org.example.BlogWebApp.controllers;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.example.BlogWebApp.entities.ErrorResponse.NO_POST_MESSAGE;

import org.example.BlogWebApp.entities.*;
import org.example.BlogWebApp.exceptions.NotFoundException;
import org.example.BlogWebApp.logging.LoggingInfo;
import org.example.BlogWebApp.mappers.*;
import org.example.SpringContainer.annotations.beans.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.SpringContainer.annotations.web.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostControllerImpl {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @LoggingInfo("Get all posts - %s")
    @GetMapping
    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    @GetMapping("/{id}")
    public Object getPostById(@PathVariable Integer id) throws JsonProcessingException {
        Post post = postMapper.getPostById(id);
        if (post == null)
            throwPostException(id, "");

        return post;
    }

    @GetMapping("/{id}/comments")
    public List<Comment> getCommentsByPostId(@PathVariable Integer id) {
        return commentMapper.getCommentsByPostId(id);
    }

    @PostMapping
    public Object createPost(@RequestBody Post post) {
        postMapper.insertPost(post);
        return post;
    }

    @PutMapping("/{id}")
    public Object updatePost(@RequestBody Post post, @PathVariable Integer id) throws JsonProcessingException {
        post.id = id;

        int affectedRows = postMapper.updatePost(post);
        if (affectedRows != 1)
            throwPostException(id, " was updated");

        return post;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public Object deletePost(@PathVariable Integer id) throws JsonProcessingException {
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
