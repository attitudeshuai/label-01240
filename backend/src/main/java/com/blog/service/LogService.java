package com.blog.service;

import com.blog.entity.OperationLog;
import com.blog.mapper.OperationLogMapper;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 日志服务
 */
@Slf4j
@Service
public class LogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 记录操作日志
     */
    @Async
    public void recordLog(String operation, String method, String params, String ip) {
        try {
            OperationLog logEntity = new OperationLog();
            logEntity.setUserId(UserContext.getUserId());
            logEntity.setUsername(UserContext.getUsername());
            logEntity.setOperation(operation);
            logEntity.setMethod(method);
            logEntity.setParams(params);
            logEntity.setIp(ip);

            operationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("记录操作日志失败: ", e);
        }
    }
}
