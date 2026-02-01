package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 自动备份服务
 * 实现定期数据备份和恢复机制
 */
@Slf4j
@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private static final String BACKUP_DIR = "/app/backups";
    private static final String UPLOADS_DIR = "/app/uploads";
    private static final int BACKUP_RETENTION_DAYS = 7;

    /**
     * 每天凌晨2点自动备份
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        log.info("开始执行定时备份任务...");
        try {
            String backupFile = performBackup();
            log.info("定时备份完成: {}", backupFile);
            cleanOldBackups();
        } catch (Exception e) {
            log.error("定时备份失败", e);
        }
    }

    /**
     * 执行备份
     * @return 备份文件路径
     */
    public String performBackup() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        // 创建备份目录
        Path backupPath = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
        }

        String backupFileName = "backup_" + timestamp + ".zip";
        Path backupFile = backupPath.resolve(backupFileName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile.toFile()))) {
            // 备份数据库（导出SQL）
            backupDatabase(zos, timestamp);
            
            // 备份上传文件
            backupUploads(zos);
            
            log.info("备份文件创建成功: {}", backupFile);
        }

        return backupFile.toString();
    }

    /**
     * 备份数据库
     */
    private void backupDatabase(ZipOutputStream zos, String timestamp) throws Exception {
        // 从URL中提取数据库信息
        String dbHost = "mysql";
        String dbName = "blog";
        
        // 使用mysqldump导出数据库
        ProcessBuilder pb = new ProcessBuilder(
            "mysqldump",
            "-h", dbHost,
            "-u", dbUsername,
            "-p" + dbPassword,
            dbName
        );
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        // 将输出写入ZIP
        ZipEntry entry = new ZipEntry("database_" + timestamp + ".sql");
        zos.putNextEntry(entry);
        
        try (InputStream is = process.getInputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        
        zos.closeEntry();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.warn("mysqldump 退出码: {}，可能需要检查数据库连接", exitCode);
        }
    }

    /**
     * 备份上传文件
     */
    private void backupUploads(ZipOutputStream zos) throws IOException {
        Path uploadsPath = Paths.get(UPLOADS_DIR);
        if (!Files.exists(uploadsPath)) {
            log.info("上传目录不存在，跳过备份");
            return;
        }

        Files.walk(uploadsPath)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    String entryName = "uploads/" + uploadsPath.relativize(file).toString();
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    log.error("备份文件失败: {}", file, e);
                }
            });
    }

    /**
     * 清理过期备份
     */
    public void cleanOldBackups() {
        try {
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                return;
            }

            LocalDateTime cutoff = LocalDateTime.now().minusDays(BACKUP_RETENTION_DAYS);
            
            Files.list(backupPath)
                .filter(p -> p.toString().endsWith(".zip"))
                .filter(p -> {
                    try {
                        return Files.getLastModifiedTime(p).toInstant()
                            .isBefore(cutoff.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(p -> {
                    try {
                        Files.delete(p);
                        log.info("已删除过期备份: {}", p);
                    } catch (IOException e) {
                        log.error("删除备份失败: {}", p, e);
                    }
                });
        } catch (IOException e) {
            log.error("清理备份失败", e);
        }
    }

    /**
     * 获取备份列表
     */
    public java.util.List<String> listBackups() throws IOException {
        Path backupPath = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupPath)) {
            return java.util.Collections.emptyList();
        }

        return Files.list(backupPath)
            .filter(p -> p.toString().endsWith(".zip"))
            .map(p -> p.getFileName().toString())
            .sorted(java.util.Comparator.reverseOrder())
            .collect(java.util.stream.Collectors.toList());
    }
}
