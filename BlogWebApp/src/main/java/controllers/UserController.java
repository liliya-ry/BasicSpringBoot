package controllers;

import SpringContainer.annotations.web.*;
import auth.Role;
import entities.User;

import java.util.List;

@RequestMapping("/users")
public interface UserController {
    @Role("admin")
    @GetMapping
    List<User> getAllUsers();

    @Role("admin")
    @GetMapping("/{username}")
    User getUserByUsername(@PathVariable String username);

    @PostMapping("/register")
    User addUser(@RequestBody User user);

    @Role("admin")
    @PutMapping("/{username}")
    Object updateUser(@RequestBody User user, @PathVariable String username);

    @Role("admin")
    @DeleteMapping("/{username}")
    Object deleteUser(@PathVariable String username);
}
