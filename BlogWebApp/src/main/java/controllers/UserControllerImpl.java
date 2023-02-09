package controllers;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static entities.ErrorResponse.NO_USER_MESSAGE;

import entities.ErrorResponse;
import entities.User;
import auth.PasswordEncryptor;
import exceptions.NotFoundException;
import mappers.UserMapper;
import SpringContainer.annotations.beans.Autowired;
import SpringContainer.annotations.beans.RestController;

import java.util.List;

@RestController
public class UserControllerImpl implements UserController {
    @Autowired
    private UserMapper userMapper;

    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    public User getUserByUsername(String username) {
        User user = userMapper.getUser(username);
        if (user == null)
            throwUserException(username, "");
        return user;
    }

    public User addUser(User user) {
        user.password = PasswordEncryptor.encryptPassword(user.password + user.generateSalt());
        userMapper.insertUser(user);
        return user;
    }

    public Object updateUser(User user, String username) {
        user.username = username;
        int affectedRows = userMapper.updateUser(user);
        if (affectedRows != 1)
            throwUserException(username, " was updated");
        return user;
    }

    public Object deleteUser(String username) {
        int affectedRows = userMapper.deleteUser(username);
        if (affectedRows != 1)
            throwUserException(username, " was deleted");
        return username;
    }

    private void throwUserException(String username, String msg) {
        ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND, NO_USER_MESSAGE + username + msg);
        throw new NotFoundException(errorResponse.toString());
    }
}
