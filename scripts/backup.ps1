# 个人博客系统备份脚本 (Windows PowerShell)
# 用法: .\scripts\backup.ps1

$ErrorActionPreference = "Stop"

# 配置
$BACKUP_DIR = ".\backups"
$DATE = Get-Date -Format "yyyyMMdd_HHmmss"
$DB_CONTAINER = "personal-blog-mysql-1"
$DB_USER = "root"
$DB_PASS = "root123"
$DB_NAME = "blog"
$BACKEND_CONTAINER = "personal-blog-backend-1"

# 创建备份目录
if (-not (Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR | Out-Null
}

Write-Host "=========================================="
Write-Host "个人博客系统备份脚本 (Windows)"
Write-Host "备份时间: $DATE"
Write-Host "=========================================="

# 1. 备份数据库
Write-Host ""
Write-Host "[1/3] 备份数据库..."
$DB_BACKUP_FILE = "$BACKUP_DIR\db_backup_$DATE.sql"

try {
    docker exec $DB_CONTAINER mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $DB_BACKUP_FILE
    Write-Host "√ 数据库备份成功: $DB_BACKUP_FILE"
    
    # 压缩备份文件
    Compress-Archive -Path $DB_BACKUP_FILE -DestinationPath "$DB_BACKUP_FILE.zip" -Force
    Remove-Item $DB_BACKUP_FILE
    Write-Host "√ 已压缩: $DB_BACKUP_FILE.zip"
} catch {
    Write-Host "× 数据库备份失败: $_"
    exit 1
}

# 2. 备份上传文件
Write-Host ""
Write-Host "[2/3] 备份上传文件..."
$UPLOADS_BACKUP_DIR = "$BACKUP_DIR\uploads_$DATE"

try {
    docker cp "${BACKEND_CONTAINER}:/app/uploads" $UPLOADS_BACKUP_DIR
    Write-Host "√ 上传文件备份成功: $UPLOADS_BACKUP_DIR"
    
    # 压缩备份目录
    Compress-Archive -Path $UPLOADS_BACKUP_DIR -DestinationPath "$UPLOADS_BACKUP_DIR.zip" -Force
    Remove-Item -Recurse -Force $UPLOADS_BACKUP_DIR
    Write-Host "√ 已压缩: $UPLOADS_BACKUP_DIR.zip"
} catch {
    Write-Host "⚠ 上传文件备份失败: $_"
}

# 3. 清理旧备份（保留最近7天）
Write-Host ""
Write-Host "[3/3] 清理旧备份..."
$cutoffDate = (Get-Date).AddDays(-7)
Get-ChildItem -Path $BACKUP_DIR -Filter "*.zip" | Where-Object { $_.LastWriteTime -lt $cutoffDate } | Remove-Item -Force
Write-Host "√ 已清理7天前的备份"

# 完成
Write-Host ""
Write-Host "=========================================="
Write-Host "备份完成！"
Write-Host "备份文件位置: $BACKUP_DIR"
Write-Host ""
Write-Host "备份文件列表:"
Get-ChildItem -Path $BACKUP_DIR -Filter "*$DATE*" | Format-Table Name, Length, LastWriteTime
Write-Host "=========================================="
