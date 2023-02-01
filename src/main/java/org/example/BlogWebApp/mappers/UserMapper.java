package org.example.BlogWebApp.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.BlogWebApp.entities.User;
import org.example.SpringContainer.annotations.beans.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    User getUser(String username);
    List<User> getAllUsers();
    void insertUser(User user);
    int updateUser(User user);
    int deleteUser(@Param("username") String username);
}
