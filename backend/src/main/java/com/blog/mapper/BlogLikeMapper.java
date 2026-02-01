package com.blog.mapper;

import com.blog.entity.BlogLike;
import org.apache.ibatis.annotations.*;

/**
 * 点赞Mapper
 */
@Mapper
public interface BlogLikeMapper {

    @Select("SELECT * FROM blog_like WHERE blog_id = #{blogId} AND user_id = #{userId}")
    BlogLike findByBlogIdAndUserId(@Param("blogId") Long blogId, @Param("userId") Long userId);

    @Insert("INSERT INTO blog_like (blog_id, user_id) VALUES (#{blogId}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BlogLike like);

    @Delete("DELETE FROM blog_like WHERE blog_id = #{blogId} AND user_id = #{userId}")
    int delete(@Param("blogId") Long blogId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM blog_like WHERE blog_id = #{blogId}")
    Integer countByBlogId(Long blogId);
}
