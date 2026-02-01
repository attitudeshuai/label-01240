# 个人博客系统部署文档

## 环境要求

### 开发环境
- JDK 24
- Maven 3.9.11
- Node.js v22.20.0
- MySQL 8.0+
- Docker & Docker Compose

### 生产环境
- Docker 20.10+
- Docker Compose 2.0+
- 2GB+ 内存
- 10GB+ 磁盘空间

---

## 快速部署 (Docker Compose)

### 1. 克隆项目

```bash
git clone <repository-url>
cd personal-blog
```

### 2. 启动服务

```bash
docker-compose up --build -d
```

### 3. 访问应用

- 前端: http://localhost:8081
- 后端API: http://localhost:8080/api

### 4. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | testuser | test1234 |

---

## 服务架构

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │  Frontend   │  │   Backend   │  │    MySQL    │     │
│  │  (Nginx)    │──│ (Spring Boot)│──│   (8.0)     │     │
│  │  Port:8081  │  │  Port:8080  │  │  Port:3306  │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
```

---

## 配置说明

### docker-compose.yml 配置

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: blog
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/init.sql

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/blog
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123
    volumes:
      - uploads:/app/uploads
    depends_on:
      mysql:
        condition: service_healthy

  frontend:
    build: ./frontend-admin
    ports:
      - "8081:80"
    depends_on:
      backend:
        condition: service_healthy
```

### 后端配置 (application.yml)

```yaml
server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

spring:
  datasource:
    url: jdbc:mysql://mysql:3306/blog?useUnicode=true&characterEncoding=utf8
    username: root
    password: root123

jwt:
  secret: your-secret-key-at-least-32-characters
  expiration: 86400000  # 24小时
```

---

## 常用命令

### 启动服务
```bash
docker-compose up -d
```

### 停止服务
```bash
docker-compose down
```

### 重新构建并启动
```bash
docker-compose down
docker-compose up --build -d
```

### 查看日志
```bash
# 所有服务
docker-compose logs -f

# 单个服务
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 进入容器
```bash
docker-compose exec backend sh
docker-compose exec mysql mysql -uroot -proot123 blog
```

### 清理数据重新开始
```bash
docker-compose down -v
docker-compose up --build -d
```

---

## 数据备份与恢复

### 备份数据库
```bash
# 使用备份脚本
./scripts/backup.sh

# 或手动备份
docker-compose exec mysql mysqldump -uroot -proot123 blog > backup.sql
```

### 恢复数据库
```bash
docker-compose exec -T mysql mysql -uroot -proot123 blog < backup.sql
```

### 备份上传文件
```bash
docker cp $(docker-compose ps -q backend):/app/uploads ./uploads_backup
```

---

## 生产环境部署建议

### 1. 安全配置

- 修改默认数据库密码
- 使用强 JWT 密钥
- 配置 HTTPS
- 限制数据库访问

### 2. 性能优化

- 配置 Nginx 缓存静态资源
- 启用 Gzip 压缩
- 配置数据库连接池
- 添加 Redis 缓存（可选）

### 3. 监控告警

- 配置健康检查
- 设置日志收集
- 配置资源监控

### 4. 高可用

- 数据库主从复制
- 应用多实例部署
- 负载均衡配置

---

## 故障排查

### 服务无法启动

1. 检查端口占用: `netstat -tlnp | grep 8080`
2. 检查 Docker 日志: `docker-compose logs backend`
3. 检查数据库连接

### 数据库连接失败

1. 确认 MySQL 服务已启动
2. 检查数据库配置
3. 验证网络连通性

### 前端无法访问后端

1. 检查 Nginx 代理配置
2. 确认后端服务健康
3. 检查 CORS 配置
