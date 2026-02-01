package com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private Integer role;
    private Integer status;
    private String avatar;
    private String bio;
    private LocalDateTime createdAt;
    
    // 统计数据
    private Integer blogCount;
    private Integer likeCount;
}
