# 个人博客系统

基于 Vue3 + Spring Boot 的前后端分离个人博客系统。

## How to Run

### Docker 一键启动（推荐）

```bash
# 构建并启动所有服务
docker-compose up --build -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 本地开发

**后端启动：**
```bash
cd backend
mvn spring-boot:run
```

**前端启动：**
```bash
cd frontend-admin
npm install
npm run dev
```

**数据库初始化：**
```bash
# 使用 Docker 启动 MySQL
docker run -d --name blog-mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=blog_db \
  -p 3306:3306 \
  mysql:8.0

# 导入表结构
docker exec -i blog-mysql mysql -uroot -proot123 blog_db < backend/src/main/resources/schema.sql
```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 8081 | Vue3 管理后台 |
| 后端 | 8080 | Spring Boot API |
| MySQL | 3306 | 数据库 |

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | test123456 | 系统管理员 |
| 普通用户 | zhangsan | test123456 | 全栈开发工程师 |
| 普通用户 | lisi | test123456 | 前端开发者 |
| 普通用户 | wangwu | test123456 | 后端架构师 |
| 普通用户 | zhaoliu | test123456 | 产品经理 |
| 普通用户 | xiaobai | test123456 | 编程新手 |

## 题目内容

开发一个基于B/S架构的个人博客系统，采用前后端分离模式。

### 核心功能模块

1. **用户认证模块 (AUTH)**
   - 用户注册：用户名唯一性验证、密码复杂度检查
   - 用户登录：身份验证、会话管理
   - 用户登出：安全退出、会话销毁

2. **博客文章模块 (BLOG)**
   - 博客发布：富文本编辑器支持(加粗、斜体、图片插入)、标题/标签设置
   - 博客编辑：作者权限验证、内容更新
   - 博客删除：软删除机制、确认对话框

3. **评论与互动模块 (INTERACT)**
   - 发表评论：纯文本评论、内容合法性验证
   - 点赞功能：单用户单博客只能点赞一次、实时计数更新

4. **个人中心模块 (PROFILE)**
   - 个人主页：用户信息展示、统计数据(文章数、点赞数)
   - 博客管理：个人博客列表、编辑删除操作

5. **后台管理模块 (ADMIN)**
   - 用户管理：用户列表查看、禁用/启用账户
   - 内容管理：全站内容审核、违规内容处理

### 技术栈

- **前端**: Vue3 + Vite + Element Plus + Pinia + Axios
- **后端**: Spring Boot 3.5 + MyBatis + MySQL 8.0
- **部署**: Docker + Docker Compose

---

## 项目结构

```
├── backend/                 # 后端服务 (Spring Boot)
│   ├── src/main/java/com/blog/
│   │   ├── controller/      # 控制器层
│   │   ├── service/         # 业务逻辑层
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── entity/          # 实体类
│   │   ├── dto/             # 数据传输对象
│   │   ├── config/          # 配置类
│   │   ├── exception/       # 全局异常处理
│   │   ├── interceptor/     # 拦截器
│   │   └── util/            # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/          # MyBatis XML
│   │   ├── schema.sql       # 数据库建表语句
│   │   └── application.yml  # 配置文件
│   ├── Dockerfile
│   └── pom.xml
├── frontend-admin/          # 前端管理后台 (Vue3)
│   ├── src/
│   │   ├── api/             # API 封装
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── views/           # 页面组件
│   │   └── styles/          # 全局样式
│   ├── Dockerfile
│   └── package.json
├── docs/                    # 项目文档
│   └── project_design.md    # 设计文档
├── docker-compose.yml
├── .gitignore
└── README.md
```

## API 接口

### 认证模块
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取当前用户信息

### 博客模块
- `GET /api/blogs` - 获取博客列表
- `GET /api/blogs/{id}` - 获取博客详情
- `POST /api/blogs` - 发布博客
- `PUT /api/blogs/{id}` - 更新博客
- `DELETE /api/blogs/{id}` - 删除博客

### 评论模块
- `GET /api/comments/blog/{blogId}` - 获取博客评论
- `POST /api/comments` - 发表评论
- `DELETE /api/comments/{id}` - 删除评论

### 点赞模块
- `POST /api/likes/blog/{blogId}` - 点赞/取消点赞
- `GET /api/likes/blog/{blogId}/status` - 获取点赞状态

### 管理模块
- `GET /api/admin/users` - 获取用户列表
- `PUT /api/admin/users/{id}/status` - 更新用户状态
- `GET /api/admin/blogs` - 获取全站博客
- `PUT /api/admin/blogs/{id}/status` - 更新博客状态
