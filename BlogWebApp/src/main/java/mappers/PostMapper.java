package mappers;

import entities.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {
    @Select("SELECT * FROM POSTS WHERE POST_ID = #{id}")
    Post getPostById(@Param("id") Integer id);

    @Select("SELECT * FROM POSTS")
    List<Post> getAllPosts();

    @Insert(value = """
    INSERT INTO POSTS(
        user_id,
        title,
        body
    )
    VALUES(
        #{userId},
        #{title},
        #{body}
    )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    void insertPost(Post post);

    @Update("""
    UPDATE POSTS
    SET
        USER_ID = #{userId},
        TITLE = #{title},
        BODY = #{body}
    WHERE POST_ID = #{postId}
    """)
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    int updatePost(Post post);

    @Delete("DELETE FROM POSTS WHERE POST_ID = #{id}")
    int deletePost(@Param("id") Integer id);
}
