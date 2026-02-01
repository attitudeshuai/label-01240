package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审核记录实体
 */
@Data
public class ReviewLog {
    private Long id;
    private String targetType;      // 审核对象类型: blog, comment, user
    private Long targetId;          // 审核对象ID
    private String action;          // 审核动作: approve, reject, hide, restore
    private String reason;          // 审核原因
    private Long reviewerId;        // 审核人ID
    private String reviewerName;    // 审核人用户名
    private LocalDateTime createdAt;
}
