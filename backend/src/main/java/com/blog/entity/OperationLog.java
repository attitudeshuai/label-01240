package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private LocalDateTime createdAt;
}
