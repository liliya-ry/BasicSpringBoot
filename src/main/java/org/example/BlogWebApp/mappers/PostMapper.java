package org.example.BlogWebApp.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.example.BlogWebApp.entities.Post;
import org.example.SpringContainer.annotations.beans.Param;
import java.util.List;

@Mapper
public interface PostMapper {
    Post getPostById(@Param("id") Integer id);
    List<Post> getAllPosts();
    void insertPost(Post post);
    int updatePost(Post post);
    int deletePost(@Param("id") Integer id);
}
