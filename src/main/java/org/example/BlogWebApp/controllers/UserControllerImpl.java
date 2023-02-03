package org.example.BlogWebApp.controllers;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.example.BlogWebApp.entities.ErrorResponse.NO_USER_MESSAGE;

import org.example.BlogWebApp.auth.*;
import org.example.BlogWebApp.entities.*;
import org.example.BlogWebApp.exceptions.NotFoundException;
import org.example.BlogWebApp.mappers.UserMapper;
import org.example.SpringContainer.annotations.beans.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.SpringContainer.annotations.web.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserControllerImpl {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Role("admin")
    @GetMapping
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Role("admin")
    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) throws JsonProcessingException {
        User user = userMapper.getUser(username);
        if (user == null)
            throwUserException(username, "");
        return user;
    }

    @PostMapping("/register")
    public User addUser(@RequestBody User user) {
        user.password = PasswordEncryptor.encryptPassword(user.password + user.generateSalt());
        userMapper.insertUser(user);
        return user;
    }

    @Role("admin")
    @PutMapping("/{username}")
    public Object updateUser(@RequestBody User user, @PathVariable String username) throws JsonProcessingException {
        user.username = username;
        int affectedRows = userMapper.updateUser(user);
        if (affectedRows != 1)
            throwUserException(username, " was updated");
        return user;
    }

    @Role("admin")
    @DeleteMapping("/{username}")
    @ResponseBody
    public Object deleteUser(@PathVariable String username) throws JsonProcessingException {
        int affectedRows = userMapper.deleteUser(username);
        if (affectedRows != 1)
            throwUserException(username, " was deleted");
        return username;
    }

    private void throwUserException(String username, String msg) throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND, NO_USER_MESSAGE + username + msg);
        String jsonError = objectMapper.writeValueAsString(errorResponse);
        throw new NotFoundException(jsonError);
    }
}
