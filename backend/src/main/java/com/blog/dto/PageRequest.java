package com.blog.dto;

import lombok.Data;

/**
 * 分页请求DTO
 */
@Data
public class PageRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Integer status;
    
    public Integer getOffset() {
        return (page - 1) * size;
    }
}
