#该配置为所有服务共用dockerfile文件，所以放在更目录用户结合Jenkinsfile配置使用
# ========== 构建阶段 ==========
FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /build

# 复制根pom和公共模块pom
COPY pom.xml .
COPY cloud-common/pom.xml cloud-common/

# 复制所有服务的pom（虽然只会用到选定的那个）
COPY cloud-consumer/pom.xml cloud-consumer/
COPY cloud-gateway/pom.xml cloud-gateway/
COPY cloud-producer/pom.xml cloud-producer/
COPY cloud-user/pom.xml cloud-user/

# 下载依赖（会被缓存）
RUN mvn dependency:go-offline -DskipTests

# 复制源代码
COPY . .

# 编译指定的服务 - SERVICE_NAME通过build-arg传入
ARG SERVICE_NAME
RUN mvn clean package -DskipTests -pl ${SERVICE_NAME} -am

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jdk

LABEL maintainer="2439534736@qq.com"

ARG BUILD_TIME
ARG VCS_REF
LABEL org.opencontainers.image.created=$BUILD_TIME
LABEL org.opencontainers.image.revision=$VCS_REF

WORKDIR /app

# 复制jar包 - SERVICE_NAME通过build-arg传入
ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

# 创建非root用户
RUN useradd -m -u 1001 appuser && chown appuser:appuser /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]