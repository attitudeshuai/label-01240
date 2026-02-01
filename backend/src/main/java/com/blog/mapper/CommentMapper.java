package com.blog.mapper;

import com.blog.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评论Mapper
 */
@Mapper
public interface CommentMapper {

    @Select("SELECT c.*, u.username, up.avatar as user_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user_profile up ON u.id = up.user_id " +
            "WHERE c.id = #{id}")
    Comment findById(Long id);

    @Insert("INSERT INTO comment (blog_id, user_id, content, status) VALUES (#{blogId}, #{userId}, #{content}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Update("UPDATE comment SET status = 0 WHERE id = #{id}")
    int delete(Long id);

    @Update("UPDATE comment SET status = #{status}, review_reason = #{reason}, " +
            "reviewed_by = #{reviewerId}, reviewed_at = NOW() WHERE id = #{id}")
    int updateStatusWithReason(@Param("id") Long id, @Param("status") Integer status,
                               @Param("reason") String reason, @Param("reviewerId") Long reviewerId);

    @Select("SELECT c.*, u.username, up.avatar as user_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user_profile up ON u.id = up.user_id " +
            "WHERE c.blog_id = #{blogId} AND c.status = 1 " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{offset}, #{size}")
    List<Comment> findByBlogId(@Param("blogId") Long blogId, @Param("offset") Integer offset, @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM comment WHERE blog_id = #{blogId} AND status = 1")
    Long countByBlogId(Long blogId);

    // 管理员查询所有评论
    List<Comment> findAllList(@Param("keyword") String keyword, @Param("status") Integer status,
                              @Param("offset") Integer offset, @Param("size") Integer size);

    Long countAllList(@Param("keyword") String keyword, @Param("status") Integer status);
}
