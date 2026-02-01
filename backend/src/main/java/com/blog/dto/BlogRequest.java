package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 博客请求DTO
 */
@Data
public class BlogRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    @Size(max = 500, message = "标签长度不能超过500")
    private String tags;
    
    private Integer status = 1; // 默认已发布
}
