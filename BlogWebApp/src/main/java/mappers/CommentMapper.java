package mappers;

import entities.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Select("SELECT * FROM COMMENTS WHERE POST_ID = #{postId}")
    List<Comment> getCommentsByPostId(@Param("postId") Integer postId);
}
