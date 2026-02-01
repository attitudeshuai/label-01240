package com.blog.service;

import com.blog.dto.*;
import com.blog.entity.User;
import com.blog.entity.UserProfile;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserProfileMapper;
import com.blog.util.JwtUtil;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);

        // 创建用户资料
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setAvatar("");
        profile.setBio("");
        userProfileMapper.insert(profile);

        log.info("用户注册成功: username={}", request.getUsername());
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());

        log.info("用户登录成功: username={}", request.getUsername());
        return result;
    }

    /**
     * 获取当前用户信息
     */
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        return getUserInfo(userId);
    }

    /**
     * 获取用户信息
     */
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserProfile profile = userProfileMapper.findByUserId(userId);

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());

        if (profile != null) {
            vo.setAvatar(profile.getAvatar());
            vo.setBio(profile.getBio());
        }

        // 统计数据
        vo.setBlogCount(blogMapper.countByAuthor(userId));
        vo.setLikeCount(blogMapper.sumLikesByAuthor(userId));

        return vo;
    }

    /**
     * 更新个人资料
     */
    public void updateProfile(ProfileRequest request) {
        Long userId = UserContext.getUserId();
        UserProfile profile = userProfileMapper.findByUserId(userId);

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setAvatar(request.getAvatar());
            profile.setBio(request.getBio());
            userProfileMapper.insert(profile);
        } else {
            profile.setAvatar(request.getAvatar());
            profile.setBio(request.getBio());
            userProfileMapper.update(profile);
        }

        log.info("用户更新资料: userId={}", userId);
    }

    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("blogCount", blogMapper.countByAuthor(userId));
        stats.put("likeCount", blogMapper.sumLikesByAuthor(userId));
        return stats;
    }

    /**
     * 获取用户列表（管理员）
     */
    public PageResult<User> getUserList(PageRequest request) {
        List<User> list = userMapper.findList(request.getKeyword(), request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = userMapper.countList(request.getKeyword(), request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 更新用户状态（管理员）
     */
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userMapper.updateStatus(userId, status);
        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
    }
}
