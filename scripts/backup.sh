#!/bin/bash

# 个人博客系统备份脚本
# 用法: ./scripts/backup.sh

set -e

# 配置
BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_CONTAINER="personal-blog-mysql-1"
DB_USER="root"
DB_PASS="root123"
DB_NAME="blog"

# 创建备份目录
mkdir -p "$BACKUP_DIR"

echo "=========================================="
echo "个人博客系统备份脚本"
echo "备份时间: $DATE"
echo "=========================================="

# 1. 备份数据库
echo ""
echo "[1/3] 备份数据库..."
DB_BACKUP_FILE="$BACKUP_DIR/db_backup_$DATE.sql"

docker exec $DB_CONTAINER mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > "$DB_BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✓ 数据库备份成功: $DB_BACKUP_FILE"
    # 压缩备份文件
    gzip "$DB_BACKUP_FILE"
    echo "✓ 已压缩: ${DB_BACKUP_FILE}.gz"
else
    echo "✗ 数据库备份失败"
    exit 1
fi

# 2. 备份上传文件
echo ""
echo "[2/3] 备份上传文件..."
UPLOADS_BACKUP_DIR="$BACKUP_DIR/uploads_$DATE"
BACKEND_CONTAINER="personal-blog-backend-1"

docker cp $BACKEND_CONTAINER:/app/uploads "$UPLOADS_BACKUP_DIR"

if [ $? -eq 0 ]; then
    echo "✓ 上传文件备份成功: $UPLOADS_BACKUP_DIR"
    # 压缩备份目录
    tar -czf "${UPLOADS_BACKUP_DIR}.tar.gz" -C "$BACKUP_DIR" "uploads_$DATE"
    rm -rf "$UPLOADS_BACKUP_DIR"
    echo "✓ 已压缩: ${UPLOADS_BACKUP_DIR}.tar.gz"
else
    echo "✗ 上传文件备份失败"
fi

# 3. 清理旧备份（保留最近7天）
echo ""
echo "[3/3] 清理旧备份..."
find "$BACKUP_DIR" -name "*.gz" -mtime +7 -delete
find "$BACKUP_DIR" -name "*.tar.gz" -mtime +7 -delete
echo "✓ 已清理7天前的备份"

# 完成
echo ""
echo "=========================================="
echo "备份完成！"
echo "备份文件位置: $BACKUP_DIR"
echo ""
echo "备份文件列表:"
ls -lh "$BACKUP_DIR"/*$DATE* 2>/dev/null || echo "无新备份文件"
echo "=========================================="
