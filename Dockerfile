# ========== 构建阶段 ==========

# ========== 运行阶段 ==========
#FROM eclipse-temurin:17-jdk
FROM eclipse-temurin:17-jre-alpine
#FROM eclipse-temurin:17-jre-jammy
LABEL maintainer="2439534736@qq.com"

ARG BUILD_TIME
ARG VCS_REF
LABEL org.opencontainers.image.created=$BUILD_TIME
LABEL org.opencontainers.image.revision=$VCS_REF

WORKDIR /app

ARG SERVICE_NAME
# 安装中文字体,如果使用轻量级alpine 镜像，需要安装中文字体，以便支持导出excel图表相关功能，没有可不用安装字体
# apk add --no-cache 确保安装后不留下安装缓存，保持镜像体积最小
# font-noto-cjk 提供了对中文/日文/韩文的良好支持
#RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
#    && apk update \
#    && apk add --no-cache font-noto-cjk
#标准 17-jdk版本创建用户
RUN #useradd -m -u 1001 appuser && chown appuser:appuser /app
# alpine 版本镜像创建用户 appuser 命令
RUN adduser -D -u 1001 appuser && chown appuser:appuser /app
USER appuser

# 🟢 修改：直接从 Jenkins 的工作目录复制已经编译好的 Jar 包
# 注意：Jenkins 编译后的路径通常在 target 下
COPY ${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]