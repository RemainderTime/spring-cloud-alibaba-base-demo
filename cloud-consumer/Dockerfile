# ========== 构建阶段 ==========
FROM maven:3.8.6-openjdk-17 AS builder

WORKDIR /build

# 先复制所有 pom.xml（公共依赖最大化缓存）
COPY pom.xml .
COPY cloud-common/pom.xml cloud-common/
COPY cloud-consumer/pom.xml cloud-consumer/
COPY cloud-gateway/pom.xml cloud-gateway/
COPY cloud-producer/pom.xml cloud-producer/
COPY cloud-user/pom.xml cloud-user/

# 下载所有依赖（只有 pom 变化时才重下）
RUN mvn dependency:go-offline -DskipTests

# ===== 关键：动态参数 + 精准复制源码（避免无关文件破坏缓存） =====
ARG SERVICE_NAME

# 复制指定服务的源码和资源
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/  # 确保覆盖
# 复制公共模块源码（通常需要）
COPY cloud-common/src cloud-common/src

# 编译指定服务
RUN mvn clean package -DskipTests -pl ${SERVICE_NAME} -am

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jdk

LABEL maintainer="2439534736@qq.com"

ARG BUILD_TIME
ARG VCS_REF
LABEL org.opencontainers.image.created=$BUILD_TIME
LABEL org.opencontainers.image.revision=$VCS_REF

WORKDIR /app

# 从 builder 阶段复制对应服务的 jar 包
ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

# 创建非 root 用户（安全最佳实践）
RUN useradd -m -u 1001 appuser && chown appuser:appuser /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]