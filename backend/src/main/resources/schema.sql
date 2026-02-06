-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- 删除旧表
DROP TABLE IF EXISTS `review_log`;
DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `blog_like`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `blog`;
DROP TABLE IF EXISTS `user_profile`;
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `role` TINYINT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户资料表
CREATE TABLE `user_profile` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `avatar` VARCHAR(500) DEFAULT '',
    `bio` VARCHAR(500) DEFAULT '',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 博客表
CREATE TABLE `blog` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `tags` VARCHAR(500) DEFAULT '',
    `author_id` BIGINT NOT NULL,
    `status` TINYINT DEFAULT 1,
    `view_count` INT DEFAULT 0,
    `like_count` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `review_status` TINYINT DEFAULT 1,
    `review_reason` VARCHAR(500) DEFAULT NULL,
    `reviewed_by` BIGINT DEFAULT NULL,
    `reviewed_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME DEFAULT NULL,
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_review_status` (`review_status`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted_at` (`deleted_at`),
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `blog_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `status` TINYINT DEFAULT 1,
    `review_reason` VARCHAR(500) DEFAULT NULL,
    `reviewed_by` BIGINT DEFAULT NULL,
    `reviewed_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_blog_id` (`blog_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`blog_id`) REFERENCES `blog`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 点赞表
CREATE TABLE `blog_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `blog_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_blog_user` (`blog_id`, `user_id`),
    INDEX `idx_blog_id` (`blog_id`),
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`blog_id`) REFERENCES `blog`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 操作日志表
CREATE TABLE `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `username` VARCHAR(50),
    `operation` VARCHAR(50) NOT NULL,
    `method` VARCHAR(200),
    `params` TEXT,
    `ip` VARCHAR(50),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 审核记录表
CREATE TABLE `review_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_type` VARCHAR(20) NOT NULL,
    `target_id` BIGINT NOT NULL,
    `action` VARCHAR(20) NOT NULL,
    `reason` VARCHAR(500) DEFAULT NULL,
    `reviewer_id` BIGINT NOT NULL,
    `reviewer_name` VARCHAR(50),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_reviewer` (`reviewer_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 初始化用户数据 (密码: test123456)
-- =====================================================
INSERT INTO `user` (`id`, `username`, `password`, `email`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$VjhjLgxZ3fdygtKzZct01.R2s.83OFhFRLKsg0M7SkmrxS/zVmII.', 'admin@blog.com', 1, 1),
(2, 'zhangsan', '$2a$10$VjhjLgxZ3fdygtKzZct01.R2s.83OFhFRLKsg0M7SkmrxS/zVmII.', 'zhangsan@example.com', 0, 1),
(3, 'lisi', '$2a$10$VjhjLgxZ3fdygtKzZct01.R2s.83OFhFRLKsg0M7SkmrxS/zVmII.', 'lisi@example.com', 0, 1),
(4, 'wangwu', '$2a$10$VjhjLgxZ3fdygtKzZct01.R2s.83OFhFRLKsg0M7SkmrxS/zVmII.', 'wangwu@example.com', 0, 1),
(5, 'zhaoliu', '$2a$10$VjhjLgxZ3fdygtKzZct01.R2s.83OFhFRLKsg0M7SkmrxS/zVmII.', 'zhaoliu@example.com', 0, 1),
(6, 'xiaobai', '$2a$10$VjhjLgxZ3fdygtKzZct01.R2s.83OFhFRLKsg0M7SkmrxS/zVmII.', 'xiaobai@example.com', 0, 1);

INSERT INTO `user_profile` (`user_id`, `avatar`, `bio`) VALUES
(1, '', '系统管理员'),
(2, '', '全栈开发工程师'),
(3, '', '前端开发者'),
(4, '', '后端架构师'),
(5, '', '产品经理'),
(6, '', '编程新手');


-- =====================================================
-- 博客文章数据
-- =====================================================
INSERT INTO `blog` (`id`, `title`, `content`, `tags`, `author_id`, `status`, `view_count`, `like_count`, `comment_count`, `created_at`) VALUES
(1, 'Spring Boot 3.0 新特性详解', 
'<h2>前言</h2>
<p>Spring Boot 3.0 是一个重大版本更新，带来了许多令人兴奋的新特性。本文将详细介绍这些变化。</p>
<h2>主要更新</h2>
<h3>1. Java 17 基线</h3>
<p>Spring Boot 3.0 要求最低 Java 17 版本，这意味着我们可以使用更多现代 Java 特性，如 Records、Sealed Classes 等。</p>
<h3>2. Jakarta EE 9+</h3>
<p>从 javax.* 迁移到 jakarta.* 命名空间，这是一个重要的变化，需要更新所有相关的导入语句。</p>
<h3>3. 原生镜像支持</h3>
<p>通过 GraalVM 原生镜像，应用启动时间可以从秒级降低到毫秒级，内存占用也大幅减少。</p>
<h2>总结</h2>
<p>Spring Boot 3.0 的这些更新为我们带来了更好的性能和开发体验，值得尽快升级。</p>', 
'Spring Boot,Java,后端开发', 2, 1, 1256, 4, 4, '2025-01-15 10:30:00'),

(2, 'Vue 3 组合式 API 最佳实践', 
'<h2>为什么选择组合式 API</h2>
<p>Vue 3 的组合式 API 提供了更灵活的代码组织方式，特别适合大型项目的开发。</p>
<h2>核心概念</h2>
<h3>ref 和 reactive</h3>
<p>ref 用于基本类型的响应式数据，reactive 用于对象类型。选择哪个取决于你的使用场景。</p>
<h3>computed 和 watch</h3>
<p>computed 用于派生状态，watch 用于执行副作用。合理使用可以让代码更清晰。</p>
<h2>实战技巧</h2>
<p>1. 使用 composables 封装可复用逻辑</p>
<p>2. 合理拆分组件，保持单一职责</p>
<p>3. 善用 TypeScript 提升代码质量</p>', 
'Vue,前端,JavaScript', 3, 1, 892, 3, 3, '2025-01-18 14:20:00'),

(3, 'MySQL 性能优化实战指南', 
'<h2>索引优化</h2>
<p>索引是数据库性能优化的关键。正确使用索引可以将查询速度提升数十倍。</p>
<h3>索引设计原则</h3>
<p>1. 选择性高的列优先建立索引</p>
<p>2. 组合索引遵循最左前缀原则</p>
<p>3. 避免在索引列上使用函数</p>
<h2>查询优化</h2>
<p>使用 EXPLAIN 分析查询计划，找出性能瓶颈。注意避免全表扫描和文件排序。</p>
<h2>配置调优</h2>
<p>根据服务器内存合理配置 innodb_buffer_pool_size，通常设置为可用内存的 70-80%。</p>', 
'MySQL,数据库,性能优化', 4, 1, 2341, 4, 4, '2025-01-20 09:00:00'),

(4, 'Docker 容器化部署完整教程', 
'<h2>Docker 基础</h2>
<p>Docker 是现代应用部署的标准方式，它提供了一致的运行环境，简化了部署流程。</p>
<h2>Dockerfile 编写</h2>
<p>一个好的 Dockerfile 应该：</p>
<p>1. 使用合适的基础镜像</p>
<p>2. 合理利用构建缓存</p>
<p>3. 减少镜像层数和大小</p>
<h2>Docker Compose</h2>
<p>对于多容器应用，Docker Compose 是最佳选择。它可以一键启动整个应用栈。</p>
<h2>生产环境建议</h2>
<p>使用健康检查、资源限制、日志管理等功能确保容器稳定运行。</p>', 
'Docker,容器化,DevOps', 2, 1, 1678, 3, 3, '2025-01-22 16:45:00'),

(5, '前端工程化：从零搭建 Vite 项目', 
'<h2>为什么选择 Vite</h2>
<p>Vite 利用浏览器原生 ES 模块，实现了极快的开发服务器启动和热更新。</p>
<h2>项目初始化</h2>
<p>使用 npm create vite@latest 快速创建项目，支持多种框架模板。</p>
<h2>配置详解</h2>
<p>vite.config.js 中可以配置：</p>
<p>1. 路径别名</p>
<p>2. 代理设置</p>
<p>3. 构建优化</p>
<h2>插件生态</h2>
<p>Vite 拥有丰富的插件生态，可以轻松集成各种工具和框架。</p>', 
'Vite,前端工程化,构建工具', 3, 1, 756, 0, 0, '2025-01-25 11:30:00');


INSERT INTO `blog` (`id`, `title`, `content`, `tags`, `author_id`, `status`, `view_count`, `like_count`, `comment_count`, `created_at`) VALUES
(6, 'Redis 缓存策略与实践', 
'<h2>缓存的重要性</h2>
<p>在高并发场景下，缓存是提升系统性能的关键手段。Redis 作为最流行的缓存方案，值得深入学习。</p>
<h2>常见缓存策略</h2>
<h3>Cache Aside</h3>
<p>最常用的策略：读时先查缓存，写时先更新数据库再删除缓存。</p>
<h3>Write Through</h3>
<p>写操作同时更新缓存和数据库，保证数据一致性。</p>
<h2>缓存问题处理</h2>
<p>1. 缓存穿透：使用布隆过滤器</p>
<p>2. 缓存击穿：使用互斥锁</p>
<p>3. 缓存雪崩：设置随机过期时间</p>', 
'Redis,缓存,高并发', 4, 1, 1890, 3, 3, '2025-01-28 08:15:00'),

(7, '我的编程学习之路', 
'<h2>入门阶段</h2>
<p>作为一个编程小白，我从 Python 开始入门。选择 Python 是因为它语法简洁，适合初学者。</p>
<h2>遇到的困难</h2>
<p>最开始理解变量、循环这些概念花了不少时间。但坚持下来后，发现编程其实很有趣。</p>
<h2>学习资源推荐</h2>
<p>1. 官方文档是最好的学习资料</p>
<p>2. 多做项目实践</p>
<p>3. 加入技术社区交流</p>
<h2>未来计划</h2>
<p>接下来准备学习 Web 开发，希望能做出自己的网站！</p>', 
'编程入门,学习心得,Python', 6, 1, 423, 3, 2, '2025-01-30 20:00:00'),

(8, '微服务架构设计要点', 
'<h2>什么是微服务</h2>
<p>微服务是一种将应用拆分为多个小型、独立服务的架构风格。每个服务专注于单一业务功能。</p>
<h2>设计原则</h2>
<p>1. 单一职责：每个服务只做一件事</p>
<p>2. 独立部署：服务之间互不影响</p>
<p>3. 去中心化：避免单点故障</p>
<h2>技术选型</h2>
<p>Spring Cloud 是 Java 生态最成熟的微服务框架，提供了完整的解决方案。</p>
<h2>注意事项</h2>
<p>微服务不是银弹，小型项目不建议过度拆分，会增加运维复杂度。</p>', 
'微服务,架构设计,Spring Cloud', 4, 1, 2156, 4, 3, '2025-02-01 10:00:00'),

(9, 'TypeScript 入门到精通', 
'<h2>为什么学习 TypeScript</h2>
<p>TypeScript 为 JavaScript 添加了类型系统，可以在编译时发现错误，提升代码质量。</p>
<h2>基础类型</h2>
<p>TypeScript 支持 string、number、boolean、array、tuple、enum 等类型。</p>
<h2>高级特性</h2>
<p>1. 泛型：编写可复用的类型安全代码</p>
<p>2. 接口：定义对象的形状</p>
<p>3. 类型推断：减少类型注解</p>
<h2>实战建议</h2>
<p>从现有 JavaScript 项目逐步迁移，不要一次性重写所有代码。</p>', 
'TypeScript,前端,JavaScript', 3, 1, 1345, 0, 0, '2025-02-03 15:30:00'),

(10, '2025 年技术趋势展望', 
'<h2>AI 与开发</h2>
<p>AI 辅助编程工具将更加成熟，但不会取代开发者，而是提升开发效率。</p>
<h2>云原生</h2>
<p>Kubernetes 生态持续发展，Serverless 架构将更加普及。</p>
<h2>前端发展</h2>
<p>React Server Components、Islands Architecture 等新模式值得关注。</p>
<h2>安全性</h2>
<p>零信任架构、供应链安全将成为重点关注领域。</p>
<h2>总结</h2>
<p>技术在不断演进，保持学习是开发者最重要的能力。</p>', 
'技术趋势,行业观察,2025', 5, 1, 3421, 4, 4, '2025-02-05 09:00:00');


-- =====================================================
-- 评论数据
-- =====================================================
INSERT INTO `comment` (`id`, `blog_id`, `user_id`, `content`, `status`, `created_at`) VALUES
(1, 1, 3, '写得很详细，Spring Boot 3.0 的变化确实很大，特别是 Jakarta EE 的迁移需要注意。', 1, '2025-01-15 12:00:00'),
(2, 1, 4, '原生镜像支持是个亮点，启动速度提升太明显了！', 1, '2025-01-15 14:30:00'),
(3, 1, 5, '请问升级过程中有什么坑需要注意的吗？', 1, '2025-01-16 09:00:00'),
(4, 1, 2, '回复楼上：主要是依赖包的兼容性问题，建议先在测试环境验证。', 1, '2025-01-16 10:15:00'),
(5, 2, 2, 'Vue 3 的组合式 API 确实比选项式 API 更灵活，代码组织更清晰。', 1, '2025-01-18 16:00:00'),
(6, 2, 4, 'composables 的封装思路很好，学习了！', 1, '2025-01-19 08:30:00'),
(7, 2, 6, '新手表示有点难理解，需要多练习。', 1, '2025-01-19 20:00:00'),
(8, 3, 2, '索引优化这块讲得很透彻，EXPLAIN 分析确实很重要。', 1, '2025-01-20 11:00:00'),
(9, 3, 3, '请问 innodb_buffer_pool_size 设置多大合适？', 1, '2025-01-20 14:00:00'),
(10, 3, 4, '回复楼上：一般是可用内存的 70-80%，具体看业务场景。', 1, '2025-01-20 15:30:00'),
(11, 3, 5, '收藏了，MySQL 优化必备知识。', 1, '2025-01-21 09:00:00'),
(12, 4, 3, 'Docker 部署确实方便，再也不用担心环境问题了。', 1, '2025-01-22 18:00:00'),
(13, 4, 5, 'Docker Compose 一键启动太爽了！', 1, '2025-01-23 10:00:00'),
(14, 4, 6, '正在学习 Docker，这篇文章帮助很大。', 1, '2025-01-23 21:00:00'),
(15, 6, 2, 'Redis 缓存策略总结得很全面，特别是缓存问题的处理方案。', 1, '2025-01-28 10:00:00'),
(16, 6, 3, '布隆过滤器防穿透是个好方案，但要注意误判率。', 1, '2025-01-28 14:00:00'),
(17, 6, 5, '实际项目中 Cache Aside 用得最多。', 1, '2025-01-29 09:00:00'),
(18, 7, 2, '加油！编程入门确实需要坚持，后面会越来越顺的。', 1, '2025-01-30 21:00:00'),
(19, 7, 3, '推荐你看看 freeCodeCamp，免费资源很多。', 1, '2025-01-31 08:00:00'),
(20, 8, 2, '微服务架构设计确实需要权衡，不能为了微服务而微服务。', 1, '2025-02-01 12:00:00'),
(21, 8, 3, 'Spring Cloud 生态确实成熟，但学习曲线也比较陡。', 1, '2025-02-01 15:00:00'),
(22, 8, 5, '小公司还是单体架构更实际。', 1, '2025-02-02 09:00:00'),
(23, 10, 2, 'AI 辅助编程确实是趋势，但核心能力还是要自己掌握。', 1, '2025-02-05 10:00:00'),
(24, 10, 3, 'Serverless 确实越来越火了，降低了运维成本。', 1, '2025-02-05 11:30:00'),
(25, 10, 4, '零信任架构是安全领域的重要方向。', 1, '2025-02-05 14:00:00'),
(26, 10, 6, '作为新手，感觉要学的东西好多啊！', 1, '2025-02-05 16:00:00');

-- =====================================================
-- 点赞数据
-- =====================================================
INSERT INTO `blog_like` (`blog_id`, `user_id`, `created_at`) VALUES
(1, 3, '2025-01-15 12:05:00'),
(1, 4, '2025-01-15 14:35:00'),
(1, 5, '2025-01-16 09:05:00'),
(1, 6, '2025-01-17 10:00:00'),
(2, 2, '2025-01-18 16:05:00'),
(2, 4, '2025-01-19 08:35:00'),
(2, 5, '2025-01-19 15:00:00'),
(3, 2, '2025-01-20 11:05:00'),
(3, 3, '2025-01-20 14:05:00'),
(3, 5, '2025-01-21 09:05:00'),
(3, 6, '2025-01-22 08:00:00'),
(4, 3, '2025-01-22 18:05:00'),
(4, 5, '2025-01-23 10:05:00'),
(4, 6, '2025-01-23 21:05:00'),
(6, 2, '2025-01-28 10:05:00'),
(6, 3, '2025-01-28 14:05:00'),
(6, 5, '2025-01-29 09:05:00'),
(7, 2, '2025-01-30 21:05:00'),
(7, 3, '2025-01-31 08:05:00'),
(7, 4, '2025-02-01 08:00:00'),
(8, 2, '2025-02-01 12:05:00'),
(8, 3, '2025-02-01 15:05:00'),
(8, 5, '2025-02-02 09:05:00'),
(8, 6, '2025-02-02 20:00:00'),
(10, 2, '2025-02-05 10:05:00'),
(10, 3, '2025-02-05 11:35:00'),
(10, 4, '2025-02-05 14:05:00'),
(10, 6, '2025-02-05 16:05:00');
