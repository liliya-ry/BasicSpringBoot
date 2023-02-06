package org.example.BlogWebApp.mappers;

import org.apache.ibatis.annotations.*;
import org.example.BlogWebApp.entities.Comment;
import java.util.List;

@Mapper
public interface CommentMapper {
    @Select("SELECT * FROM COMMENTS WHERE POST_ID = #{postId}")
    List<Comment> getCommentsByPostId(@Param("postId") Integer postId);
}
