# ========== 构建阶段 ==========
FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /build

# 第1步：先复制所有pom.xml（这层会被缓存）
COPY pom.xml .
COPY cloud-common/pom.xml cloud-common/
COPY cloud-gateway/pom.xml cloud-gateway/

# 第2步：下载依赖（这层会被缓存，只要pom.xml没改就不重新下载）
RUN mvn dependency:go-offline -DskipTests

# 第3步：再复制所有源代码
COPY . .

# 第4步：编译项目（依赖已缓存，只需要编译源代码）
RUN mvn clean package -DskipTests -pl cloud-gateway -am

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jdk

LABEL maintainer="2439534736@qq.com"

ARG BUILD_TIME
ARG VCS_REF
LABEL org.opencontainers.image.created=$BUILD_TIME
LABEL org.opencontainers.image.revision=$VCS_REF

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /build/cloud-gateway/target/cloud-gateway-*.jar app.jar

# 创建非root用户（安全性）
RUN useradd -m -u 1001 appuser && chown appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]