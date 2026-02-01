package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户资料实体
 */
@Data
public class UserProfile {
    private Long id;
    private Long userId;
    private String avatar;
    private String bio;
    private LocalDateTime updatedAt;
}
