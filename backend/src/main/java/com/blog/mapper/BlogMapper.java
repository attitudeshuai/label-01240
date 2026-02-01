package com.blog.mapper;

import com.blog.entity.Blog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 博客Mapper
 */
@Mapper
public interface BlogMapper {

    @Select("SELECT b.*, u.username as author_name, up.avatar as author_avatar " +
            "FROM blog b " +
            "LEFT JOIN user u ON b.author_id = u.id " +
            "LEFT JOIN user_profile up ON u.id = up.user_id " +
            "WHERE b.id = #{id} AND b.deleted_at IS NULL")
    Blog findById(Long id);

    @Insert("INSERT INTO blog (title, content, tags, author_id, status) VALUES (#{title}, #{content}, #{tags}, #{authorId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Blog blog);

    @Update("UPDATE blog SET title = #{title}, content = #{content}, tags = #{tags}, status = #{status} WHERE id = #{id}")
    int update(Blog blog);

    @Update("UPDATE blog SET deleted_at = NOW() WHERE id = #{id}")
    int softDelete(Long id);

    @Update("<script>UPDATE blog SET deleted_at = NOW() WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int batchSoftDelete(@Param("ids") java.util.List<Long> ids);

    @Update("UPDATE blog SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Update("UPDATE blog SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") Integer delta);

    @Update("UPDATE blog SET comment_count = comment_count + #{delta} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Long id, @Param("delta") Integer delta);

    @Update("UPDATE blog SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE blog SET status = #{status}, review_status = #{status}, review_reason = #{reason}, " +
            "reviewed_by = #{reviewerId}, reviewed_at = NOW() WHERE id = #{id}")
    int updateStatusWithReason(@Param("id") Long id, @Param("status") Integer status, 
                               @Param("reason") String reason, @Param("reviewerId") Long reviewerId);

    List<Blog> findList(@Param("keyword") String keyword, @Param("status") Integer status,
                        @Param("offset") Integer offset, @Param("size") Integer size);

    Long countList(@Param("keyword") String keyword, @Param("status") Integer status);

    List<Blog> findByAuthorId(@Param("authorId") Long authorId, @Param("status") Integer status,
                              @Param("offset") Integer offset, @Param("size") Integer size);

    Long countByAuthorId(@Param("authorId") Long authorId, @Param("status") Integer status);

    // 管理员查询（包含所有状态）
    List<Blog> findAllList(@Param("keyword") String keyword, @Param("status") Integer status,
                           @Param("offset") Integer offset, @Param("size") Integer size);

    Long countAllList(@Param("keyword") String keyword, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM blog WHERE author_id = #{authorId} AND deleted_at IS NULL")
    Integer countByAuthor(Long authorId);

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM blog WHERE author_id = #{authorId} AND deleted_at IS NULL")
    Integer sumLikesByAuthor(Long authorId);
}
