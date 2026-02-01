package com.blog.controller;

import com.blog.dto.*;
import com.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserInfo(@PathVariable Long id) {
        UserVO user = userService.getUserInfo(id);
        return Result.success(user);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileRequest request) {
        userService.updateProfile(request);
        return Result.success("更新成功", null);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long id) {
        Map<String, Object> stats = userService.getUserStats(id);
        return Result.success(stats);
    }
}
