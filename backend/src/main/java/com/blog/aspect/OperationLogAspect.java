package com.blog.aspect;

import com.blog.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志AOP切面
 * 自动记录Controller层的操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private LogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    // 操作名称映射
    private static final Map<String, String> OPERATION_MAP = new HashMap<>();
    static {
        // 认证相关
        OPERATION_MAP.put("register", "用户注册");
        OPERATION_MAP.put("login", "用户登录");
        OPERATION_MAP.put("logout", "用户登出");
        // 博客相关
        OPERATION_MAP.put("createBlog", "创建博客");
        OPERATION_MAP.put("updateBlog", "更新博客");
        OPERATION_MAP.put("deleteBlog", "删除博客");
        // 评论相关
        OPERATION_MAP.put("createComment", "发表评论");
        OPERATION_MAP.put("deleteComment", "删除评论");
        // 点赞相关
        OPERATION_MAP.put("like", "点赞");
        OPERATION_MAP.put("unlike", "取消点赞");
        // 用户相关
        OPERATION_MAP.put("updateProfile", "更新个人资料");
        OPERATION_MAP.put("uploadAvatar", "上传头像");
        OPERATION_MAP.put("uploadImage", "上传图片");
        // 管理相关
        OPERATION_MAP.put("toggleUserStatus", "切换用户状态");
        OPERATION_MAP.put("toggleBlogStatus", "切换博客状态");
        OPERATION_MAP.put("adminDeleteComment", "管理员删除评论");
    }

    /**
     * 切入点：所有Controller的公共方法（排除GET请求的查询方法）
     */
    @Pointcut("execution(public * com.blog.controller..*Controller.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String httpMethod = request.getMethod();

        // GET请求不记录日志（查询操作）
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return joinPoint.proceed();
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 获取操作名称
        String operation = OPERATION_MAP.getOrDefault(methodName, methodName);

        // 获取请求参数
        String params = "";
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤敏感信息
                params = filterSensitiveParams(args);
            }
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
        }

        // 获取客户端IP
        String ip = getClientIp(request);

        // 完整方法名
        String fullMethodName = className + "." + methodName;

        // 执行目标方法
        Object result = joinPoint.proceed();

        // 异步记录日志
        logService.recordLog(operation, fullMethodName, params, ip);

        return result;
    }

    /**
     * 过滤敏感参数（如密码）
     */
    private String filterSensitiveParams(Object[] args) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (arg == null) continue;
                // 跳过HttpServletRequest/Response等对象
                String className = arg.getClass().getName();
                if (className.contains("HttpServlet") || className.contains("MultipartFile")) {
                    continue;
                }
                String json = objectMapper.writeValueAsString(arg);
                // 过滤密码字段
                json = json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
                sb.append(json).append("; ");
            }
            String result = sb.toString();
            // 限制长度
            if (result.length() > 500) {
                result = result.substring(0, 500) + "...";
            }
            return result;
        } catch (Exception e) {
            return "参数解析失败";
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
