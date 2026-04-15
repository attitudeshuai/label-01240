# AuthInterceptor 设计逻辑分析

## 一、登出用户带旧token请求 `/api/blogs` 完整链路

### 链路执行顺序（完全对应 AuthInterceptor.java preHandle 方法判断顺序）
1. **请求进入拦截器**：用户携带已登出的token发送GET请求到 `/api/blogs`
2. **OPTIONS请求判断**：不是OPTIONS请求，继续执行
3. **获取请求信息**：uri=`/api/blogs`，method=`GET`，token=`Bearer <已登出的token>`
4. **公开GET接口判断**：满足`"GET".equalsIgnoreCase(method)`且uri匹配公开路径规则，`isPublicGet = true`
5. **token存在性判断**：token有值，继续执行
6. **去除Bearer前缀**：token变为`<已登出的token>`
7. **黑名单检查**：`tokenBlacklistService.isBlacklisted(token)`返回true（token已登出）
8. **公开接口特殊处理**：由于`isPublicGet = true`，直接`return true`放行请求
9. **跳过后续逻辑**：不会执行token有效性校验，也不会执行`UserContext.setUserId()`等用户上下文设置
10. **请求进入Controller**：用户上下文未设置
11. **请求结束后清理**：`afterCompletion`方法执行`UserContext.clear()`，没有副作用

### 最终结果
请求正常放行，接口返回博客列表数据，但是**整个请求生命周期内 UserContext 没有任何用户信息**。

---

### 放行后完整业务链路（preHandle -> Controller -> Service -> 返回）
1. **进入BlogController**：请求匹配到 `@GetMapping` 注解的 `getBlogList` 方法，接收分页参数 `PageRequest`
2. **调用BlogService.getBlogList**：Controller直接把分页参数透传给Service层的公开博客列表方法
3. **Service层执行查询**：方法内部直接调用 `blogMapper.findList()` 进行数据库查询，只用到了分页参数、关键词和状态过滤参数，**全程没有调用UserContext获取用户信息**
4. **封装返回结果**：查询到博客列表数据和总条数后，封装成PageResult返回给Controller
5. **Controller返回响应**：Controller把结果包装成统一的Result格式返回给前端

### 为什么这条链路不依赖UserContext
公开博客列表本身就是面向所有用户的内容，不需要根据登录用户做个性化处理：
- 查询逻辑只过滤公开状态的博客，不需要根据用户ID做权限过滤
- 返回的博客数据是公共属性（标题、内容、作者、发布时间、浏览量、点赞数等），所有用户看到的内容都是一致的
- 整个查询流程不需要获取当前登录用户的任何信息，所以UserContext是否为空对结果没有任何影响

## 二、UserContext未设置对后续业务逻辑的影响

### 1. 仅做查询的公开接口（如GET /api/blogs列表接口）
- 影响：无问题，接口只需要返回公开的博客数据，不需要依赖用户身份
- 表现：正常返回列表，用户可以正常浏览内容

### 2. 需要感知用户身份的公开接口（如获取点赞状态）
以 `LikeService.getLikeStatus(Long blogId)` 为例：
- 代码逻辑：`Long userId = UserContext.getUserId();` 直接获取用户ID
- 问题：UserContext未设置时，`getUserId()`会返回`null`
- 表现：查询点赞状态时，`blogLikeMapper.findByBlogIdAndUserId(blogId, null)`找不到记录，返回`liked: false`，即使用户实际之前点赞过，也会显示未点赞状态

### 3. 需要用户身份的非公开操作（如点赞接口）
以 `LikeService.toggleLike(Long blogId)` 为例：
- 路径说明：点赞接口是POST请求，不属于公开GET接口，所以在拦截器阶段就会被拦截返回401，不会进入Service层
- 但如果有业务漏洞让这类请求进入Service层：`userId = null`会导致插入点赞记录时`user_id`字段为空，或者抛出数据库非空约束异常

## 三、设计的取舍权衡

### 1. 收益
- **用户体验优化**：已登出用户或者带无效token的用户仍然可以正常浏览公开内容，不会被强制跳转到登录页
- **前端开发友好**：前端不需要在登出后清除所有请求的token，也不需要对公开接口做特殊处理，降低前端复杂度
- **性能提升**：公开接口不需要做完整的token校验和用户上下文设置，减少不必要的计算
- **兼容性好**：支持客户端缓存token的场景，即使token过期也不影响公开内容浏览

### 2. 代价
- **逻辑一致性风险**：同一个接口在匿名访问和登录访问时表现不一致（如点赞状态显示），需要前端做好适配
- **业务逻辑复杂度增加**：所有依赖UserContext的公开接口都需要做null值判断，处理匿名访问场景
- **潜在的安全风险**：如果某个接口应该是需要认证的，但被错误配置到了公开GET列表中，会导致权限泄露
- **调试难度增加**：用户上下文为空的场景需要单独测试，容易出现空指针异常

### 3. 适用场景
这种设计适合**内容优先的公开类应用**（如博客、资讯网站），核心目标是让用户可以无阻碍地浏览内容，登录状态只影响互动类操作。
