package com.blog.mapper;

import com.blog.entity.ReviewLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 审核记录Mapper
 */
@Mapper
public interface ReviewLogMapper {

    @Insert("INSERT INTO review_log (target_type, target_id, action, reason, reviewer_id, reviewer_name) " +
            "VALUES (#{targetType}, #{targetId}, #{action}, #{reason}, #{reviewerId}, #{reviewerName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ReviewLog log);

    @Select("SELECT * FROM review_log WHERE target_type = #{targetType} AND target_id = #{targetId} " +
            "ORDER BY created_at DESC")
    List<ReviewLog> findByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT * FROM review_log ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<ReviewLog> findAll(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM review_log")
    Long count();
}
