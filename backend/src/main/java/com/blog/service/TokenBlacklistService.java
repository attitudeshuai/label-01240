package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token黑名单服务
 * 用于实现服务端会话销毁
 */
@Slf4j
@Service
public class TokenBlacklistService {

    // 存储被加入黑名单的token及其过期时间
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 将token加入黑名单
     * @param token JWT token
     * @param expirationTime token的过期时间戳
     */
    public void addToBlacklist(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
        log.info("Token已加入黑名单");
    }

    /**
     * 检查token是否在黑名单中
     * @param token JWT token
     * @return true表示在黑名单中（已失效）
     */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * 定时清理过期的黑名单token（每小时执行一次）
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int removed = 0;
        for (Map.Entry<String, Long> entry : blacklist.entrySet()) {
            if (entry.getValue() < now) {
                blacklist.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.info("清理了{}个过期的黑名单token", removed);
        }
    }
}
