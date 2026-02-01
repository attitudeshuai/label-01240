package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 点赞实体
 */
@Data
public class BlogLike {
    private Long id;
    private Long blogId;
    private Long userId;
    private LocalDateTime createdAt;
}
