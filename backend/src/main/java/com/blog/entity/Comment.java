package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体
 */
@Data
public class Comment {
    private Long id;
    private Long blogId;
    private Long userId;
    private String content;
    private Integer status;     // 0-已删除, 1-正常
    private LocalDateTime createdAt;
    
    // 关联字段
    private String username;
    private String userAvatar;
}
