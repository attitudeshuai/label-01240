package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.Blog;
import com.blog.entity.Comment;
import com.blog.entity.ReviewLog;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ReviewLogMapper;
import com.blog.service.BlogService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReviewLogMapper reviewLogMapper;

    /**
     * 验证管理员权限
     */
    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    /**
     * 记录审核日志
     */
    private void recordReviewLog(String targetType, Long targetId, String action, String reason) {
        ReviewLog log = new ReviewLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setReason(reason);
        log.setReviewerId(UserContext.getUserId());
        log.setReviewerName(UserContext.getUsername());
        reviewLogMapper.insert(log);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<PageResult<User>> getUserList(PageRequest request) {
        checkAdmin();
        PageResult<User> result = userService.getUserList(request);
        return Result.success(result);
    }

    /**
     * 更新用户状态（带审核原因）
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id, 
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        checkAdmin();
        userService.updateUserStatus(id, status);
        
        String action = status == 1 ? "restore" : "hide";
        recordReviewLog("user", id, action, reason);
        
        return Result.success("更新成功", null);
    }

    /**
     * 获取全站博客列表
     */
    @GetMapping("/blogs")
    public Result<PageResult<Blog>> getAllBlogs(PageRequest request) {
        checkAdmin();
        PageResult<Blog> result = blogService.getAllBlogs(request);
        return Result.success(result);
    }

    /**
     * 更新博客状态（带审核原因）
     */
    @PutMapping("/blogs/{id}/status")
    public Result<Void> updateBlogStatus(
            @PathVariable Long id, 
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        checkAdmin();
        blogService.updateBlogStatusWithReason(id, status, reason, UserContext.getUserId());
        
        String action = status == 1 ? "approve" : "reject";
        recordReviewLog("blog", id, action, reason);
        
        return Result.success("更新成功", null);
    }

    /**
     * 获取全站评论列表
     */
    @GetMapping("/comments")
    public Result<PageResult<Comment>> getAllComments(PageRequest request) {
        checkAdmin();
        PageResult<Comment> result = commentService.getAllComments(request);
        return Result.success(result);
    }

    /**
     * 删除评论（带审核原因）
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        checkAdmin();
        commentService.adminDeleteComment(id, reason, UserContext.getUserId());
        
        recordReviewLog("comment", id, "hide", reason);
        
        return Result.success("删除成功", null);
    }

    /**
     * 获取审核记录
     */
    @GetMapping("/review-logs")
    public Result<PageResult<ReviewLog>> getReviewLogs(PageRequest request) {
        checkAdmin();
        List<ReviewLog> list = reviewLogMapper.findAll(request.getOffset(), request.getSize());
        Long total = reviewLogMapper.count();
        return Result.success(PageResult.of(list, total, request.getPage(), request.getSize()));
    }

    /**
     * 获取指定对象的审核历史
     */
    @GetMapping("/review-logs/{targetType}/{targetId}")
    public Result<List<ReviewLog>> getReviewHistory(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        checkAdmin();
        List<ReviewLog> logs = reviewLogMapper.findByTarget(targetType, targetId);
        return Result.success(logs);
    }
}
