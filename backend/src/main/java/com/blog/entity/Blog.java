package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 博客实体
 */
@Data
public class Blog {
    private Long id;
    private String title;
    private String content;
    private String tags;
    private Long authorId;
    private Integer status;     // 0-草稿, 1-已发布, 2-已下架
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // 关联字段
    private String authorName;
    private String authorAvatar;
}
