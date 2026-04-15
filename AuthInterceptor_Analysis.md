# AuthInterceptor 认证拦截器深度分析

## 一、问题场景：登出用户携带黑名单 token 请求 /api/blogs

### 完整执行链路（结合 AuthInterceptor.java 代码路径）

用户登出后，带着旧的黑名单 token 请求 GET /api/blogs 接口，执行流程如下：

第 1 步：进入 preHandle 方法，对应 AuthInterceptor.java 第 40 行，请求到达拦截器，开始执行认证逻辑。

第 2 步：OPTIONS 请求判断，对应 AuthInterceptor.java 第 42 到 44 行。实际是 GET 请求，跳过这个判断，继续执行。

第 3 步：获取请求信息，对应 AuthInterceptor.java 第 46 到 48 行。拿到请求 URI 是 /api/blogs，请求方法是 GET，以及请求头中的旧 token。

第 4 步：判断是否为公开 GET 接口，对应 AuthInterceptor.java 第 51 行。因为请求方法是 GET，并且 URI 在 PUBLIC_GET_PATTERNS 公开路径列表中，所以 isPublicGet 标记为 true。

第 5 步：检查 token 是否存在，对应 AuthInterceptor.java 第 54 到 60 行。token 存在，跳过这个分支，继续执行。

第 6 步：处理 Bearer 前缀，对应 AuthInterceptor.java 第 63 到 65 行。去除 token 开头的 Bearer 字符串，处理完成。

第 7 步：检查 token 是否在黑名单，对应 AuthInterceptor.java 第 68 到 73 行。这是核心逻辑点：token 确实在黑名单中，但因为 isPublicGet 是 true，所以直接执行 return true 放行。关键结果是：方法在此处返回，后续所有代码包括设置 UserContext 的代码全部跳过！

第 8 步：Controller 层执行，对应 BlogController.java 第 25 到 29 行。调用 blogService.getBlogList 方法获取博客列表。

第 9 步：Service 层执行，对应 BlogService.java 第 29 到 34 行。这个方法不依赖 UserContext，正常查询博客列表并返回。

最终结果：用户成功获取到博客列表数据，HTTP 状态码 200，但整个请求生命周期中，UserContext.getUserId、UserContext.getUsername、UserContext.getRole 全部为 null。

---

## 二、非 GET 请求（写操作）的执行路径修正

之前的分析有误，createBlog、updateBlog、deleteBlog 这些写操作不会遇到 UserContext 为空的问题。让我们看登出用户带黑名单 token 请求 POST /api/blogs（发布博客）的真实路径：

第 1 步：同样进入 preHandle 方法。

第 2 步：OPTIONS 请求判断，跳过。

第 3 步：获取请求信息，URI 是 /api/blogs，请求方法是 POST。

第 4 步：判断是否为公开 GET 接口，因为请求方法是 POST 不是 GET，所以 isPublicGet 标记为 false。

第 5 步：检查 token 是否存在，token 存在，继续执行。

第 6 步：处理 Bearer 前缀，完成。

第 7 步：检查 token 是否在黑名单，对应 AuthInterceptor.java 第 68 到 73 行。token 在黑名单中，但此时 isPublicGet 是 false，所以不会走放行分支，直接抛出 401 异常，提示"登录已失效，请重新登录"。

最终结果：请求在拦截器层就被拦下了，根本到不了 Controller 和 Service 层，所以不会执行到 createBlog 方法，也就不存在 UserContext 为空的风险。

同理，PUT、DELETE 等所有非 GET 请求，只要 token 在黑名单或无效，都会在这一步直接抛出异常，不会继续执行。

---

## 三、UserContext 未设置对业务逻辑的真实影响

只有公开 GET 接口可能遇到 UserContext 为空的情况，让我们逐一分析：

### 1. 对 LikeService.toggleLike 的影响

这个方法首先获取 UserContext.getUserId，如果为空就是 null。然后用这个 null 去查询该用户是否已经点赞。

具体影响：findByBlogIdAndUserId 方法会执行 user_id IS NULL 的查询。理论上数据库中不会有 user_id 为 null 的点赞记录，所以查不到。但更关键的是，toggleLike 对应的接口是 POST 请求，按照刚才的分析，请求在拦截器层就会被拦下，根本执行不到这里。

所以 toggleLike 实际上不会遇到 UserContext 为空的问题。

### 2. 对 LikeService.getLikeStatus 的影响

这个方法同样获取 UserContext.getUserId，如果为空就是 null。然后查询点赞状态。

具体影响：永远返回 liked = false，因为找不到 user_id 为 null 的点赞记录。关键是，getLikeStatus 通常是 GET 请求，比如 GET /api/blogs/123/like-status，这个路径如果在公开列表中，就会被放行，此时 UserContext 为空。

结果就是：即使用户原本有点赞记录，也显示未点赞，前端点赞状态显示不一致。

### 3. 对 BlogService.getMyBlogs 的影响

这个方法获取 UserContext.getUserId，然后查询该用户的博客列表。

具体影响：查询 author_id 为 null 的博客，大概率返回空列表，用户看不到自己的博客。但要注意，getMyBlogs 对应的接口是 GET /api/blogs/my，这个路径不在 PUBLIC_GET_PATTERNS 列表中。所以 isPublicGet 是 false，请求会要求认证，token 无效时直接抛出异常，不会执行到这里。

### 4. 对 BlogService.getBlogList 和 getBlogDetail 的影响

这两个方法不依赖 UserContext，所以 UserContext 为空对它们完全没有影响，正常返回博客数据。这也是当前设计的主要目标：公开内容可以正常浏览。

---

## 四、设计取舍分析

这种设计到底在取舍什么？

### 优点（取）

第一，简化匿名访问逻辑。公开 GET 接口统一放行，不需要区分"没有 token"和"token 无效"两种情况，代码逻辑简洁，减少分支判断。

第二，用户体验优化。登出用户浏览公开内容时不会被打断，token 过期用户仍可正常浏览博客，不会突然跳转到登录页，浏览体验流畅。

第三，降低认证服务压力。黑名单 token 不需要走完整的 JWT 验证流程，公开接口直接放行，减少了 token 验证的计算开销。

第四，API 设计一致性。公开接口对登录用户、未登录用户、登出用户表现一致，不会因为 token 状态不同而返回不同的 HTTP 状态码。

### 缺点（舍）

第一，状态不一致问题。前端可能还显示已登录状态因为本地还有 token，但实际用户已登出，依赖用户身份的功能如点赞会静默失败或状态错误。

第二，调试困难。UserContext 为 null 导致的问题可能在深层业务逻辑中才暴露，难以追溯是真匿名访问还是 token 失效但被放行。

第三，安全边界模糊。token 黑名单机制在公开接口上形同虚设，虽然是公开数据，但设计上存在逻辑不一致。

第四，需要额外防御编程。对于既可能被匿名访问又依赖用户身份的接口如点赞状态查询，需要在 Service 层额外处理 UserContext 为 null 的情况。

---

## 五、代码设计建议

### 当前设计的核心矛盾

AuthInterceptor.java 第 68 到 73 行以及第 75 到 81 行的逻辑是：token 黑名单或无效时，公开 GET 接口直接放行，但跳过了 UserContext 的设置。

### 优化方向

方案 A：明确设置匿名标记。在放行时给 UserContext 设置一个明确的匿名标记，而不是什么都不设置，这样业务层可以清楚区分是真匿名还是 token 失效。

方案 B：清理无效 token。在放行时通过响应头告诉前端这个 token 已经无效，让前端主动清理本地 token，避免状态不一致。

方案 C：Service 层防御性编程。每个依赖 UserContext 的方法开头增加校验，如果 userId 为 null 就抛出异常或返回默认值。不过要注意，写操作接口实际上在拦截器层已经被保护了，主要是读操作中依赖用户身份的接口需要处理。

---

## 六、总结

关键结论：

第一，执行路径。登出用户带黑名单 token 请求公开 GET 接口时，在 AuthInterceptor.java 第 70 行直接 return true，跳过 UserContext 设置。非 GET 请求在同一步直接抛出 401 异常，不会继续执行。

第二，业务影响。只有公开 GET 接口中依赖 UserContext 的读操作如点赞状态查询会受到影响，返回错误状态。所有写操作在拦截器层就被拦下，不会遇到 UserContext 为空的问题。

第三，设计取舍。取的是用户体验流畅、代码简洁、公开接口行为一致；舍的是状态一致性、调试便利性、部分接口的准确性。

第四，本质。这是一个典型的用户体验优先 vs 系统严谨性的权衡决策。当前设计偏向用户体验，让登出或 token 过期的用户可以无缝继续浏览内容。但需要注意在少数几个既公开又依赖用户身份的读接口中做好空值处理，避免状态显示错误。
