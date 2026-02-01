-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户资料表
CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    `bio` VARCHAR(500) DEFAULT '' COMMENT '个人简介',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 博客表
CREATE TABLE IF NOT EXISTS `blog` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `tags` VARCHAR(500) DEFAULT '' COMMENT '标签(逗号分隔)',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT DEFAULT 0 COMMENT '评论次数',
    `review_status` TINYINT DEFAULT 1 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已拒绝',
    `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因/违规原因',
    `reviewed_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_review_status` (`review_status`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted_at` (`deleted_at`),
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `blog_id` BIGINT NOT NULL COMMENT '博客ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常, 2-违规隐藏',
    `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因/违规原因',
    `reviewed_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_blog_id` (`blog_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`blog_id`) REFERENCES `blog`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `blog_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `blog_id` BIGINT NOT NULL COMMENT '博客ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_blog_user` (`blog_id`, `user_id`),
    INDEX `idx_blog_id` (`blog_id`),
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`blog_id`) REFERENCES `blog`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '用户ID',
    `username` VARCHAR(50) COMMENT '用户名',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `method` VARCHAR(200) COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 审核记录表
CREATE TABLE IF NOT EXISTS `review_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_type` VARCHAR(20) NOT NULL COMMENT '审核对象类型: blog, comment, user',
    `target_id` BIGINT NOT NULL COMMENT '审核对象ID',
    `action` VARCHAR(20) NOT NULL COMMENT '审核动作: approve, reject, hide, restore',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
    `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
    `reviewer_name` VARCHAR(50) COMMENT '审核人用户名',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_reviewer` (`reviewer_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 插入默认管理员账号 (密码: admin123)
-- BCrypt hash for 'admin123': $2a$10$EqKcp1WFKVQISheBxkVJceXf1AoNaFfigVVSPFKpzSX1U.x1qquku
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`) VALUES 
('admin', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1AoNaFfigVVSPFKpzSX1U.x1qquku', 'admin@blog.com', 1, 1)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 插入管理员资料
INSERT INTO `user_profile` (`user_id`, `avatar`, `bio`) 
SELECT id, '', '系统管理员' FROM `user` WHERE `username` = 'admin'
ON DUPLICATE KEY UPDATE `bio` = 'admin';

-- 插入测试用户 (密码: test1234)
-- BCrypt hash for 'test1234': $2a$10$EqKcp1WFKVQISheBxkVJceXf1AoNaFfigVVSPFKpzSX1U.x1qquku
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`) VALUES 
('testuser', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1AoNaFfigVVSPFKpzSX1U.x1qquku', 'test@blog.com', 0, 1)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 插入测试用户资料
INSERT INTO `user_profile` (`user_id`, `avatar`, `bio`) 
SELECT id, '', '测试用户' FROM `user` WHERE `username` = 'testuser'
ON DUPLICATE KEY UPDATE `bio` = 'test';
