package org.example.BlogWebApp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.BlogWebApp.auth.Role;
import org.example.BlogWebApp.entities.User;
import org.example.SpringContainer.annotations.web.*;

import java.util.List;

@RequestMapping("/users")
public interface UserController {
    @Role("admin")
    @GetMapping
    List<User> getAllUsers();

    @Role("admin")
    @GetMapping("/{username}")
    User getUserByUsername(@PathVariable String username) throws JsonProcessingException;

    @PostMapping("/register")
    User addUser(@RequestBody User user);

    @Role("admin")
    @PutMapping("/{username}")
    Object updateUser(@RequestBody User user, @PathVariable String username) throws JsonProcessingException;

    @Role("admin")
    @DeleteMapping("/{username}")
    Object deleteUser(@PathVariable String username) throws JsonProcessingException;
}
