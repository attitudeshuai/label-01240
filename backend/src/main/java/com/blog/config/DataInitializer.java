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
 * 数据初始化器 - 确保管理员账号存在
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
        // 只在admin不存在时创建，不覆盖已有数据
        initAdminIfNotExists();
    }

    private void initAdminIfNotExists() {
        User admin = userMapper.findByUsername("admin");
        
        if (admin == null) {
            // 创建管理员，密码: test123456
            String correctPassword = passwordEncoder.encode("test123456");
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
            
            log.info("创建管理员账号: admin / test123456");
        } else {
            log.info("管理员账号已存在，跳过初始化");
        }
    }
}
