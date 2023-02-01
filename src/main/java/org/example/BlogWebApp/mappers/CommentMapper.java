package org.example.BlogWebApp.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.BlogWebApp.entities.Comment;
import org.example.SpringContainer.annotations.beans.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> getCommentsByPostId(@Param("postId") Integer postId);
}
