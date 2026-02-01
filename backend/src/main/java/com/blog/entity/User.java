package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Integer role;       // 0-普通用户, 1-管理员
    private Integer status;     // 0-禁用, 1-正常
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
