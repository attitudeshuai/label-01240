package com.blog.controller;

import com.blog.dto.Result;
import com.blog.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 点赞控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/blog/{blogId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long blogId) {
        Map<String, Object> result = likeService.toggleLike(blogId);
        return Result.success(result);
    }

    /**
     * 获取点赞状态
     */
    @GetMapping("/blog/{blogId}/status")
    public Result<Map<String, Object>> getLikeStatus(@PathVariable Long blogId) {
        Map<String, Object> result = likeService.getLikeStatus(blogId);
        return Result.success(result);
    }
}
