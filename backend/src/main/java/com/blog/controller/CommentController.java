package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取博客评论列表
     */
    @GetMapping("/blog/{blogId}")
    public Result<PageResult<Comment>> getCommentsByBlogId(@PathVariable Long blogId, PageRequest request) {
        PageResult<Comment> result = commentService.getCommentsByBlogId(blogId, request);
        return Result.success(result);
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Comment> createComment(@Valid @RequestBody CommentRequest request) {
        Comment comment = commentService.createComment(request);
        return Result.success("评论成功", comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功", null);
    }
}
