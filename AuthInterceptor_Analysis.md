# AuthInterceptor 认证拦截器深度分析

## 一、三类请求的完整执行链路对比

### 第一类：公开 GET 请求（以 GET /api/blogs 为例，token 已登出）

第一步，OPTIONS 请求检查，请求方法是 GET，不是 OPTIONS，继续执行。

第二步，获取请求信息，uri 是 /api/blogs，method 是 GET，从 Authorization header 中获取旧 token（用户已登出，token 已加入黑名单）。

第三步，判断是否为公开 GET 接口，结果为 true，因为 /api/blogs 在公开 GET 路径列表中。

第四步，检查 token 是否存在，token 存在，跳过这个分支，继续执行。

第五步，去掉 Bearer 前缀，如果 token 以 "Bearer " 开头，截取后面的内容。

第六步，检查 token 是否在黑名单中，结果返回 true（已登出）。进入判断：因为是公开 GET 接口，所以直接放行。

关键就在这里：此处直接放行，不会执行后续的 UserContext 设置代码。

第七步，拦截器放行，请求进入 Controller。此时 UserContext 完全没有设置，ThreadLocal 中没有任何用户信息。

### 第二类：非公开 GET 请求（以 GET /api/likes/blog/1/status 为例，token 已登出）

第一步，OPTIONS 请求检查，请求方法是 GET，不是 OPTIONS，继续执行。

第二步，获取请求信息，uri 是 /api/likes/blog/1/status，method 是 GET，从 Authorization header 中获取旧 token。

第三步，判断是否为公开 GET 接口，结果为 false，因为 /api/likes 不在公开 GET 路径列表中。

第四步，检查 token 是否存在，token 存在，跳过这个分支，继续执行。

第五步，去掉 Bearer 前缀。

第六步，检查 token 是否在黑名单中，结果返回 true（已登出）。进入判断：因为不是公开 GET 接口，所以抛出 401 异常，登录已失效。

请求在此处被拦截，不会进入 Controller 和 Service 层。

### 第三类：非 GET 请求（以 POST /api/likes/blog/1 为例，token 已登出）

第一步，OPTIONS 请求检查，请求方法是 POST，不是 OPTIONS，继续执行。

第二步，获取请求信息，uri 是 /api/likes/blog/1，method 是 POST，从 Authorization header 中获取旧 token。

第三步，判断是否为公开 GET 接口，结果为 false，因为方法不是 GET。

第四步，检查 token 是否存在，token 存在，跳过这个分支，继续执行。

第五步，去掉 Bearer 前缀。

第六步，检查 token 是否在黑名单中，结果返回 true（已登出）。进入判断：因为不是公开 GET 接口，所以抛出 401 异常，登录已失效。

请求在此处被拦截，不会进入 Controller 和 Service 层。

---

## 二、if 判断顺序的关键作用

preHandle 方法中的判断顺序非常重要，从上到下依次是：

1. OPTIONS 请求，直接放行
2. 无 token 且是公开 GET，放行
3. 无 token 且不是公开 GET，抛出 401
4. token 在黑名单且是公开 GET，放行
5. token 在黑名单且不是公开 GET，抛出 401
6. token 无效且是公开 GET，放行
7. token 无效且不是公开 GET，抛出 401
8. token 有效，设置 UserContext，放行

设计意图：把"公开接口放行"的判断放在最前面，确保公开接口无论 token 状态如何都能快速返回，避免不必要的 token 验证开销。

---

## 三、真实风险边界分析

### 为什么 toggleLike 不属于当前设计下的真实风险

toggleLike 对应的接口是 POST /api/likes/blog/{blogId}，这是一个 POST 请求，不是 GET 请求。

在第三步判断 isPublicGet 时，因为方法不是 GET，所以 isPublicGet 直接为 false。

在第六步检查到 token 在黑名单时，因为 isPublicGet 是 false，所以会直接抛出 401 异常，请求被拦截。

因此，toggleLike 方法根本不会被执行到 Service 层，不存在 userId 为 null 的问题。

### 为什么 getLikeStatus 也不会被放行到 Service

getLikeStatus 对应的接口是 GET /api/likes/blog/{blogId}/status，虽然是 GET 请求，但路径是 /api/likes 开头。

公开 GET 路径列表只包含 /api/blogs、/api/comments/blog/、/api/users/，不包含 /api/likes。

在第三步判断 isPublicGet 时，因为路径不在公开列表中，所以 isPublicGet 为 false。

在第六步检查到 token 在黑名单时，会直接抛出 401 异常，请求被拦截，不会进入 Service 层。

### 为什么 getMyBlogs 是真实的风险点

getMyBlogs 对应的接口是 GET /api/blogs/my，这是一个 GET 请求，且路径是 /api/blogs 开头。

公开 GET 路径列表包含 /api/blogs，路径匹配逻辑是 uri.equals(pattern) 或 uri.startsWith(pattern)。

所以 /api/blogs/my 会匹配到 /api/blogs 这个 pattern，isPublicGet 为 true。

在第六步检查到 token 在黑名单时，因为 isPublicGet 是 true，所以直接放行，不会设置 UserContext。

请求会进入 Controller 层，然后调用 Service 层的 getMyBlogs 方法，此时 UserContext.getUserId() 返回 null。

---

## 四、真正受影响的接口范围

真正受影响的接口需要同时满足三个条件：

1. 是 GET 请求
2. 路径在公开 GET 路径列表中（或匹配前缀）
3. 接口内部依赖 UserContext 获取用户信息

根据代码分析，真正受影响的接口包括：

1. GET /api/blogs/my - 获取我的博客列表，依赖 UserContext.getUserId()
2. GET /api/blogs/{id} - 获取博客详情（如果后续添加个性化逻辑，如"我是否点赞"，会受影响）
3. GET /api/comments/blog/{blogId} - 获取博客评论（如果有个性化逻辑）
4. GET /api/users/{id} - 获取用户信息（如果有个性化逻辑）

需要特别注意的是：GET /api/blogs/my 这个接口，它的路径 /api/blogs/my 匹配了 /api/blogs 前缀，所以会被当作公开接口放行，但它本质上是一个需要登录的个性化接口。

---

## 五、UserContext 未设置对真实受影响接口的影响

### 对 GET /api/blogs/my 的影响

getMyBlogs 方法中调用 UserContext.getUserId() 返回 null。

查询数据库时使用 null 作为作者 ID，SQL 条件变成 author_id = null，这在 SQL 中永远不会匹配到任何记录。

返回结果是空列表，不会报错，但逻辑上不正确。用户看到的是一片空白，而不是自己的博客列表。

### 对 GET /api/blogs/{id} 的影响（假设有个性化逻辑）

如果博客详情接口后续添加"我是否点赞"的标记，调用 getLikeStatus 时 userId 为 null。

查询点赞记录时传入 null，要么查不到（返回未点赞），要么查到所有 null 的点赞记录（逻辑混乱）。

### 对纯公开读接口的影响

GET /api/blogs（博客列表）不依赖 UserContext，正常工作，无任何问题。

GET /api/blogs/{id}（博客详情，纯公开查询）不依赖 UserContext，正常返回博客详情，浏览次数正常增加。

---

## 六、设计取舍分析

### 这样设计想要得到什么

优点 1：简化前端逻辑，前端不需要判断 token 是否有效，统一携带即可，公开页面即使 token 过期也能正常浏览，用户体验流畅。

优点 2：接口复用性，同一个接口既支持匿名访问，也支持登录用户访问，比如博客列表，登录用户可能看到"我是否点赞"的标记，匿名用户看不到。

优点 3：性能优化，公开接口不需要验证 token 有效性，减少了 JWT 解密和数据库查询开销，黑名单检查只对非公开接口强制执行。

优点 4：容错性强，token 过期或失效时，用户仍然能浏览公开内容，不会被强制跳转到登录页。

### 这样设计牺牲了什么

问题 1：代码一致性被破坏，同一条链路中，UserContext 可能存在也可能不存在，每个 Service 方法都需要考虑 userId 为 null 的情况，增加了心智负担。

问题 2：路径匹配的粒度问题，使用前缀匹配会导致一些本应需要登录的接口（如 /api/blogs/my）被误判为公开接口。

问题 3：调试困难，线上出现 userId = null 导致的空列表时，很难排查是哪里出的问题，因为拦截器层面没有报错，请求"正常"执行了。

问题 4：权限边界模糊，原本需要登录的个性化接口（如"我的博客"），在 token 失效时也能执行，但返回的结果是错误的。

---

## 七、总结

### 核心矛盾
这是一个典型的"用户体验 vs 代码严谨性"的权衡：

选择了用户体验：让已登出或 token 失效的用户能无缝继续浏览公开内容。

牺牲了代码严谨性：UserContext 的存在性没有保证，路径前缀匹配可能导致权限边界模糊。

### 建议的改进方向
如果要保留这种设计，需要补充：

1. 细化公开路径匹配，不要使用宽泛的前缀匹配，而是精确列出每个公开接口，或者将个性化接口（如 /api/blogs/my）移到其他路径前缀下。

2. Service 层防御性编程，在需要 userId 的方法开头校验 UserContext.getUserId() 不为 null，为 null 时抛出 401。

3. ThreadLocal 初始值，UserContext.getUserId() 未设置时抛出异常而不是返回 null，让问题尽早暴露。

4. 路径匹配优化，使用 Ant 风格的路径匹配模式，如 /api/blogs/**，但排除 /api/blogs/my。
