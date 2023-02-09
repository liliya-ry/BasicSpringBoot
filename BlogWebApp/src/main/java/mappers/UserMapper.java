package mappers;

import entities.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM USERS WHERE USERNAME = #{username}")
    User getUser(String username);

    @Select("SELECT * FROM USERS")
    List<User> getAllUsers();

    @Insert("""
    INSERT INTO USERS(
        username,
        password,
        email,
        first_name,
        last_name,
        salt,
        role
    )
    VALUES(
        #{username},
        #{password},
        #{email},
        #{firstName},
        #{lastName},
        #{salt},
        #{role}
    )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);

    @Update("""
    UPDATE USERS
        SET
        PASSWORD = #{password},
        EMAIL = #{email},
        FIRST_NAME = #{firstName},
        LAST_NAME = #{lastName},
        SALT = #{salt},
        ROLE = #{role}
        WHERE USERNAME = #{username}
     """)
    @Options(useGeneratedKeys = true, keyProperty = "username")
    int updateUser(User user);

    @Delete("DELETE FROM USERS WHERE USERNAME = #{username}")
    int deleteUser(String username);
}
