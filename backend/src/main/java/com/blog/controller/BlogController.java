package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.Blog;
import com.blog.service.BlogService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 博客控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 获取博客列表（公开）
     */
    @GetMapping
    public Result<PageResult<Blog>> getBlogList(PageRequest request) {
        PageResult<Blog> result = blogService.getBlogList(request);
        return Result.success(result);
    }

    /**
     * 获取博客详情
     */
    @GetMapping("/{id}")
    public Result<Blog> getBlogDetail(@PathVariable Long id) {
        Blog blog = blogService.getBlogDetail(id);
        return Result.success(blog);
    }

    /**
     * 发布博客
     */
    @PostMapping
    public Result<Blog> createBlog(@Valid @RequestBody BlogRequest request) {
        Blog blog = blogService.createBlog(request);
        return Result.success("发布成功", blog);
    }

    /**
     * 更新博客
     */
    @PutMapping("/{id}")
    public Result<Blog> updateBlog(@PathVariable Long id, @Valid @RequestBody BlogRequest request) {
        Blog blog = blogService.updateBlog(id, request);
        return Result.success("更新成功", blog);
    }

    /**
     * 删除博客
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量删除博客
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteBlogs(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的博客");
        }
        blogService.batchDeleteBlogs(ids);
        return Result.success("批量删除成功", null);
    }

    /**
     * 获取用户博客列表
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<Blog>> getUserBlogs(@PathVariable Long userId, PageRequest request) {
        PageResult<Blog> result = blogService.getUserBlogs(userId, request);
        return Result.success(result);
    }

    /**
     * 获取我的博客列表
     */
    @GetMapping("/my")
    public Result<PageResult<Blog>> getMyBlogs(PageRequest request) {
        PageResult<Blog> result = blogService.getMyBlogs(request);
        return Result.success(result);
    }
}
