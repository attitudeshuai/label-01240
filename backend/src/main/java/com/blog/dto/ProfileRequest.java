package com.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人资料请求DTO
 */
@Data
public class ProfileRequest {
    @Size(max = 500, message = "头像URL不能超过500字符")
    private String avatar;
    
    @Size(max = 500, message = "个人简介不能超过500字")
    private String bio;
}
