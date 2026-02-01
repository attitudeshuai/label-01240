-- 修复密码脚本
-- 密码: admin123 和 test1234 的 BCrypt 哈希值

USE blog_db;

-- 更新管理员密码 (admin123)
UPDATE `user` SET `password` = '$2a$10$EqKcp1WFKVQISheBxkVJceXf1AoNaFfigVVSPFKpzSX1U.x1qquku' WHERE `username` = 'admin';

-- 更新测试用户密码 (test1234)
UPDATE `user` SET `password` = '$2a$10$EqKcp1WFKVQISheBxkVJceXf1AoNaFfigVVSPFKpzSX1U.x1qquku' WHERE `username` = 'testuser';

-- 验证更新
SELECT id, username, password FROM `user`;
