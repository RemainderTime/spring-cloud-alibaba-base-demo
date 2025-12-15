# ========== 构建阶段 ==========

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jdk
LABEL maintainer="2439534736@qq.com"

ARG BUILD_TIME
ARG VCS_REF
LABEL org.opencontainers.image.created=$BUILD_TIME
LABEL org.opencontainers.image.revision=$VCS_REF

WORKDIR /app

ARG SERVICE_NAME

# 🟢 修改：直接从 Jenkins 的工作目录复制已经编译好的 Jar 包
# 注意：Jenkins 编译后的路径通常在 target 下
COPY ${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

RUN useradd -m -u 1001 appuser && chown appuser:appuser /app
USER appuser

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]