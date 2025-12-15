# ========== 构建阶段 ==========
FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /build

# 复制所有 pom.xml（保持依赖缓存最大化）
COPY pom.xml .
COPY cloud-common/pom.xml cloud-common/
COPY cloud-consumer/pom.xml cloud-consumer/
COPY cloud-gateway/pom.xml cloud-gateway/
COPY cloud-producer/pom.xml cloud-producer/
COPY cloud-user/pom.xml cloud-user/

# 下载所有依赖（只有 pom 变化时才重下）
RUN mvn dependency:go-offline -DskipTests

# ===== 关键：只复制指定服务的源码和资源 =====
ARG SERVICE_NAME

# 复制指定服务的源码（精准，避免无关文件影响缓存）
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/
# 如果有 resources 单独配置，可加：
# COPY ${SERVICE_NAME}/src/main/resources ${SERVICE_NAME}/src/main/resources

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

ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

RUN useradd -m -u 1001 appuser && chown appuser:appuser /app
USER appuser

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]