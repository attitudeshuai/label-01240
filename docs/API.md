# 个人博客系统 API 文档

## 概述

- 基础URL: `http://localhost:8080/api`
- 认证方式: JWT Bearer Token
- 响应格式: JSON

## 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 1. 认证模块 (Auth)

### 1.1 用户注册

**POST** `/api/auth/register`

**请求体:**
```json
{
  "username": "string (4-20字符)",
  "password": "string (8-20字符，包含字母和数字)"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 1.2 用户登录

**POST** `/api/auth/login`

**请求体:**
```json
{
  "username": "string",
  "password": "string"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "JWT Token",
    "user": {
      "id": 1,
      "username": "admin",
      "role": 1,
      "avatar": "/uploads/avatars/xxx.jpg",
      "bio": "个人简介"
    }
  }
}
```

### 1.3 用户登出

**POST** `/api/auth/logout`

**请求头:** `Authorization: Bearer {token}`

**响应:**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 1.4 获取当前用户信息

**GET** `/api/auth/info`

**请求头:** `Authorization: Bearer {token}`

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "role": 1,
    "avatar": "/uploads/avatars/xxx.jpg",
    "bio": "个人简介"
  }
}
```

---

## 2. 博客模块 (Blog)

### 2.1 获取博客列表

**GET** `/api/blogs`

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |
| keyword | string | 否 | 搜索关键词 |
| tag | string | 否 | 标签筛选 |
| authorId | long | 否 | 作者ID筛选 |

**响应:**
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "title": "博客标题",
        "content": "博客内容",
        "tags": "Vue,前端",
        "authorId": 1,
        "authorName": "admin",
        "authorAvatar": "/uploads/avatars/xxx.jpg",
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "createdAt": "2026-01-30T10:00:00",
        "updatedAt": "2026-01-30T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 2.2 获取博客详情

**GET** `/api/blogs/{id}`

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "博客标题",
    "content": "博客内容（HTML）",
    "tags": "Vue,前端",
    "authorId": 1,
    "authorName": "admin",
    "authorAvatar": "/uploads/avatars/xxx.jpg",
    "viewCount": 100,
    "likeCount": 10,
    "commentCount": 5,
    "liked": false,
    "createdAt": "2026-01-30T10:00:00"
  }
}
```

### 2.3 创建博客

**POST** `/api/blogs`

**请求头:** `Authorization: Bearer {token}`

**请求体:**
```json
{
  "title": "博客标题",
  "content": "博客内容（HTML）",
  "tags": "Vue,前端",
  "status": 1
}
```

**响应:**
```json
{
  "code": 200,
  "message": "发布成功",
  "data": { "id": 1 }
}
```

### 2.4 更新博客

**PUT** `/api/blogs/{id}`

**请求头:** `Authorization: Bearer {token}`

**请求体:**
```json
{
  "title": "博客标题",
  "content": "博客内容",
  "tags": "Vue,前端"
}
```

### 2.5 删除博客

**DELETE** `/api/blogs/{id}`

**请求头:** `Authorization: Bearer {token}`

---

## 3. 评论模块 (Comment)

### 3.1 获取博客评论

**GET** `/api/comments/blog/{blogId}`

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |

### 3.2 发表评论

**POST** `/api/comments`

**请求头:** `Authorization: Bearer {token}`

**请求体:**
```json
{
  "blogId": 1,
  "content": "评论内容"
}
```

### 3.3 删除评论

**DELETE** `/api/comments/{id}`

**请求头:** `Authorization: Bearer {token}`

---

## 4. 点赞模块 (Like)

### 4.1 点赞博客

**POST** `/api/likes/{blogId}`

**请求头:** `Authorization: Bearer {token}`

### 4.2 取消点赞

**DELETE** `/api/likes/{blogId}`

**请求头:** `Authorization: Bearer {token}`

### 4.3 检查是否已点赞

**GET** `/api/likes/{blogId}/check`

**请求头:** `Authorization: Bearer {token}`

---

## 5. 用户模块 (User)

### 5.1 获取用户主页信息

**GET** `/api/users/{id}`

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "avatar": "/uploads/avatars/xxx.jpg",
    "bio": "个人简介",
    "blogCount": 10,
    "likeCount": 100,
    "createdAt": "2026-01-01T00:00:00"
  }
}
```

### 5.2 更新个人资料

**PUT** `/api/users/profile`

**请求头:** `Authorization: Bearer {token}`

**请求体:**
```json
{
  "avatar": "/uploads/avatars/xxx.jpg",
  "bio": "个人简介"
}
```

---

## 6. 文件上传模块 (Upload)

### 6.1 上传头像

**POST** `/api/upload/avatar`

**请求头:** 
- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**请求体:** `file: 图片文件 (最大5MB, 支持jpg/png/gif/webp)`

**响应:**
```json
{
  "code": 200,
  "data": {
    "url": "/uploads/avatars/xxx.jpg"
  }
}
```

### 6.2 上传博客图片

**POST** `/api/upload/image`

**请求头:** 
- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**请求体:** `file: 图片文件 (最大5MB)`

---

## 7. 管理模块 (Admin)

> 需要管理员权限 (role=1)

### 7.1 获取用户列表

**GET** `/api/admin/users`

### 7.2 切换用户状态

**PUT** `/api/admin/users/{id}/status`

### 7.3 获取所有博客

**GET** `/api/admin/blogs`

### 7.4 切换博客状态

**PUT** `/api/admin/blogs/{id}/status`

### 7.5 获取所有评论

**GET** `/api/admin/comments`

### 7.6 删除评论

**DELETE** `/api/admin/comments/{id}`
