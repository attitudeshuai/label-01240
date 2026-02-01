package com.blog.config;

import com.blog.entity.User;
import com.blog.entity.UserProfile;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserProfileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 确保测试账号密码正确
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        initAdminUser();
        initTestUser();
    }

    private void initAdminUser() {
        User admin = userMapper.findByUsername("admin");
        String correctPassword = passwordEncoder.encode("admin123");
        
        if (admin == null) {
            // 创建管理员
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(correctPassword);
            admin.setEmail("admin@blog.com");
            admin.setRole(1);
            admin.setStatus(1);
            userMapper.insert(admin);
            
            // 创建资料
            UserProfile profile = new UserProfile();
            profile.setUserId(admin.getId());
            profile.setAvatar("");
            profile.setBio("系统管理员");
            userProfileMapper.insert(profile);
            
            log.info("创建管理员账号: admin / admin123");
        } else {
            // 更新密码
            admin.setPassword(correctPassword);
            userMapper.updatePassword(admin.getId(), correctPassword);
            log.info("更新管理员密码: admin / admin123");
        }
    }

    private void initTestUser() {
        User testUser = userMapper.findByUsername("testuser");
        String correctPassword = passwordEncoder.encode("test1234");
        
        if (testUser == null) {
            // 创建测试用户
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(correctPassword);
            testUser.setEmail("test@blog.com");
            testUser.setRole(0);
            testUser.setStatus(1);
            userMapper.insert(testUser);
            
            // 创建资料
            UserProfile profile = new UserProfile();
            profile.setUserId(testUser.getId());
            profile.setAvatar("");
            profile.setBio("测试用户");
            userProfileMapper.insert(profile);
            
            log.info("创建测试用户: testuser / test1234");
        } else {
            // 更新密码
            testUser.setPassword(correctPassword);
            userMapper.updatePassword(testUser.getId(), correctPassword);
            log.info("更新测试用户密码: testuser / test1234");
        }
    }
}
