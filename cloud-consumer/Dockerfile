## 使用官方 JDK 运行环境
#FROM eclipse-temurin:17-jdk
#
## 作者信息（可选）
#LABEL maintainer="2439534736@qq.com"
#
## 切换工作目录
#WORKDIR /app
#
## 拷贝 jar 包到容器
#COPY target/*.jar app.jar
#
## 暴露端口（建议写死或和配置一致）
#EXPOSE 9092
#
## 启动命令，可根据实际情况添加 JVM 参数
#ENTRYPOINT ["java", "-jar", "app.jar"]

# ========== 构建阶段 ==========
FROM maven:3.8.6-openjdk-17 AS builder

WORKDIR /build

# 复制整个项目
COPY . .

# 下载依赖
RUN mvn dependency:go-offline -DskipTests

# 构建项目（替换{SERVICE_NAME}为你的服务名，如cloud-consumer）
RUN mvn clean package -DskipTests -pl cloud-consumer -am

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jdk

LABEL maintainer="2439534736@qq.com"

ARG BUILD_TIME
ARG VCS_REF
LABEL org.opencontainers.image.created=$BUILD_TIME
LABEL org.opencontainers.image.revision=$VCS_REF

WORKDIR /app

# 从构建阶段复制jar包（替换{SERVICE_NAME}为你的服务名）
COPY --from=builder /build/cloud-consumer/target/cloud-consumer-*.jar app.jar

# 创建非root用户（安全性）
RUN useradd -m -u 1000 appuser && chown appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
