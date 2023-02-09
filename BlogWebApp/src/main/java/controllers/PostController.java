package controllers;

import SpringContainer.annotations.web.*;
import entities.*;
import logging.LoggingInfo;

import java.util.List;

@RequestMapping("/posts")
public interface PostController {
    @LoggingInfo("Get all posts - %s")
    @GetMapping
    List<Post> getAllPosts();

    @GetMapping("/{id}")
    Object getPostById(@PathVariable Integer id);

    @GetMapping("/{id}/comments")
    List<Comment> getCommentsByPostId(@PathVariable Integer id);

    @PostMapping
    Object createPost(@RequestBody Post post);

    @PutMapping("/{id}")
    Object updatePost(@RequestBody Post post, @PathVariable Integer id);

    @DeleteMapping("/{id}")
    Object deletePost(@PathVariable Integer id);
}
