# 使用官方 JDK 运行环境
FROM eclipse-temurin:17-jdk

# 作者信息（可选）
LABEL maintainer="2439534736@qq.com"

# 切换工作目录
WORKDIR /app

# 拷贝 jar 包到容器
COPY target/*.jar app.jar

# 暴露端口（建议写死或和配置一致）
EXPOSE 9090

# 启动命令，可根据实际情况添加 JVM 参数
ENTRYPOINT ["java", "-jar", "app.jar"]
