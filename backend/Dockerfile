# 多阶段构建 - 支持 ARM 和 X86
FROM --platform=$BUILDPLATFORM maven:3.9.11-eclipse-temurin-24 AS builder

WORKDIR /app

# 设置编码
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# 复制 pom.xml 和 maven-settings.xml
COPY pom.xml .
COPY maven-settings.xml .
RUN mvn dependency:go-offline -B -s maven-settings.xml

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B -Dfile.encoding=UTF-8 -s maven-settings.xml

# 运行阶段
FROM eclipse-temurin:24-jre

WORKDIR /app

# 安装 curl 用于健康检查
RUN (sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list || true) && \
    (sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list || true) && \
    (sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list.d/ubuntu.sources || true) && \
    (sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list.d/ubuntu.sources || true) && \
    apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 设置编码
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# 创建上传目录
RUN mkdir -p /app/uploads/avatars /app/uploads/images

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
