package org.example.BlogWebApp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.BlogWebApp.entities.*;
import org.example.BlogWebApp.logging.LoggingInfo;
import org.example.SpringContainer.annotations.web.*;

import java.util.List;

@RequestMapping("/posts")
public interface PostController {
    @LoggingInfo("Get all posts - %s")
    @GetMapping
    List<Post> getAllPosts();

    @GetMapping("/{id}")
    Object getPostById(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping("/{id}/comments")
    List<Comment> getCommentsByPostId(@PathVariable Integer id);

    @PostMapping
    Object createPost(@RequestBody Post post);

    @PutMapping("/{id}")
    Object updatePost(@RequestBody Post post, @PathVariable Integer id) throws JsonProcessingException;

    @DeleteMapping("/{id}")
    @ResponseBody
    Object deletePost(@PathVariable Integer id) throws JsonProcessingException;
}
