# 个人博客系统恢复脚本 (Windows PowerShell)
# 用法: .\scripts\restore.ps1 <备份日期>
# 示例: .\scripts\restore.ps1 20260130_120000

param(
    [Parameter(Mandatory=$false)]
    [string]$BackupDate
)

$ErrorActionPreference = "Stop"

# 配置
$BACKUP_DIR = ".\backups"
$DB_CONTAINER = "personal-blog-mysql-1"
$DB_USER = "root"
$DB_PASS = "root123"
$DB_NAME = "blog"
$BACKEND_CONTAINER = "personal-blog-backend-1"

# 检查参数
if (-not $BackupDate) {
    Write-Host "用法: .\scripts\restore.ps1 <备份日期>"
    Write-Host "示例: .\scripts\restore.ps1 20260130_120000"
    Write-Host ""
    Write-Host "可用的备份:"
    Get-ChildItem -Path $BACKUP_DIR -Filter "db_backup_*.sql.zip" | ForEach-Object {
        $_.Name -replace "db_backup_", "" -replace ".sql.zip", ""
    }
    exit 1
}

Write-Host "=========================================="
Write-Host "个人博客系统恢复脚本 (Windows)"
Write-Host "恢复备份: $BackupDate"
Write-Host "=========================================="

# 确认操作
Write-Host ""
Write-Host "警告: 此操作将覆盖当前数据！"
$confirm = Read-Host "确定要继续吗？(y/N)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "已取消"
    exit 0
}

# 1. 恢复数据库
Write-Host ""
Write-Host "[1/2] 恢复数据库..."
$DB_BACKUP_FILE = "$BACKUP_DIR\db_backup_$BackupDate.sql.zip"

if (-not (Test-Path $DB_BACKUP_FILE)) {
    Write-Host "× 数据库备份文件不存在: $DB_BACKUP_FILE"
    exit 1
}

try {
    # 解压
    $tempDir = [System.IO.Path]::GetTempPath() + [System.Guid]::NewGuid().ToString()
    Expand-Archive -Path $DB_BACKUP_FILE -DestinationPath $tempDir -Force
    $sqlFile = Get-ChildItem -Path $tempDir -Filter "*.sql" | Select-Object -First 1
    
    # 恢复
    Get-Content $sqlFile.FullName | docker exec -i $DB_CONTAINER mysql -u$DB_USER -p$DB_PASS $DB_NAME
    
    # 清理
    Remove-Item -Recurse -Force $tempDir
    
    Write-Host "√ 数据库恢复成功"
} catch {
    Write-Host "× 数据库恢复失败: $_"
    exit 1
}

# 2. 恢复上传文件
Write-Host ""
Write-Host "[2/2] 恢复上传文件..."
$UPLOADS_BACKUP_FILE = "$BACKUP_DIR\uploads_$BackupDate.zip"

if (Test-Path $UPLOADS_BACKUP_FILE) {
    try {
        $tempDir = [System.IO.Path]::GetTempPath() + [System.Guid]::NewGuid().ToString()
        Expand-Archive -Path $UPLOADS_BACKUP_FILE -DestinationPath $tempDir -Force
        
        $uploadsDir = Get-ChildItem -Path $tempDir -Directory | Select-Object -First 1
        docker cp "$($uploadsDir.FullName)\." "${BACKEND_CONTAINER}:/app/uploads/"
        
        Remove-Item -Recurse -Force $tempDir
        Write-Host "√ 上传文件恢复成功"
    } catch {
        Write-Host "⚠ 上传文件恢复失败: $_"
    }
} else {
    Write-Host "⚠ 上传文件备份不存在，跳过"
}

Write-Host ""
Write-Host "=========================================="
Write-Host "恢复完成！"
Write-Host "=========================================="
