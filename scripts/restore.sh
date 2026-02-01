#!/bin/bash

# 个人博客系统恢复脚本
# 用法: ./scripts/restore.sh <备份日期>
# 示例: ./scripts/restore.sh 20260130_120000

set -e

# 配置
BACKUP_DIR="./backups"
DB_CONTAINER="personal-blog-mysql-1"
DB_USER="root"
DB_PASS="root123"
DB_NAME="blog"
BACKEND_CONTAINER="personal-blog-backend-1"

# 检查参数
if [ -z "$1" ]; then
    echo "用法: ./scripts/restore.sh <备份日期>"
    echo "示例: ./scripts/restore.sh 20260130_120000"
    echo ""
    echo "可用的备份:"
    ls -1 "$BACKUP_DIR"/*.sql.gz 2>/dev/null | sed 's/.*db_backup_//' | sed 's/.sql.gz//' || echo "无可用备份"
    exit 1
fi

DATE=$1

echo "=========================================="
echo "个人博客系统恢复脚本"
echo "恢复备份: $DATE"
echo "=========================================="

# 确认操作
echo ""
echo "警告: 此操作将覆盖当前数据！"
read -p "确定要继续吗？(y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "已取消"
    exit 0
fi

# 1. 恢复数据库
echo ""
echo "[1/2] 恢复数据库..."
DB_BACKUP_FILE="$BACKUP_DIR/db_backup_$DATE.sql.gz"

if [ ! -f "$DB_BACKUP_FILE" ]; then
    echo "✗ 数据库备份文件不存在: $DB_BACKUP_FILE"
    exit 1
fi

# 解压并恢复
gunzip -c "$DB_BACKUP_FILE" | docker exec -i $DB_CONTAINER mysql -u$DB_USER -p$DB_PASS $DB_NAME

if [ $? -eq 0 ]; then
    echo "✓ 数据库恢复成功"
else
    echo "✗ 数据库恢复失败"
    exit 1
fi

# 2. 恢复上传文件
echo ""
echo "[2/2] 恢复上传文件..."
UPLOADS_BACKUP_FILE="$BACKUP_DIR/uploads_$DATE.tar.gz"

if [ -f "$UPLOADS_BACKUP_FILE" ]; then
    # 解压到临时目录
    TEMP_DIR=$(mktemp -d)
    tar -xzf "$UPLOADS_BACKUP_FILE" -C "$TEMP_DIR"
    
    # 复制到容器
    docker cp "$TEMP_DIR/uploads_$DATE/." $BACKEND_CONTAINER:/app/uploads/
    
    # 清理临时目录
    rm -rf "$TEMP_DIR"
    
    echo "✓ 上传文件恢复成功"
else
    echo "⚠ 上传文件备份不存在，跳过"
fi

# 完成
echo ""
echo "=========================================="
echo "恢复完成！"
echo "=========================================="
