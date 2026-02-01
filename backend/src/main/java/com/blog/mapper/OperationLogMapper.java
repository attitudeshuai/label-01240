package com.blog.mapper;

import com.blog.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_log (user_id, username, operation, method, params, ip) " +
            "VALUES (#{userId}, #{username}, #{operation}, #{method}, #{params}, #{ip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLog log);
}
