pipeline {
    agent any

    // ========== 参数化构建 ==========
    parameters {
        choice(
            name: 'SERVICE_NAME',
            choices: [
                'cloud-consumer',
                'cloud-gateway',
                'cloud-producer',
                'cloud-user'
            ],
            description: '选择要构建的微服务'
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        // ========== 需要修改的静态配置（保持为字符串常量） ==========
        DOCKER_REGISTRY = "registry.cn-hangzhou.aliyuncs.com"
        DOCKER_NAMESPACE = "xf-spring-cloud-alibaba"
        DOCKER_CREDENTIALS_ID = "aliyun-docker-credentials"

        GITHUB_REPO = "https://github.com/RemainderTime/spring-cloud-alibaba-base-demo.git"
        GITHUB_CREDENTIALS_ID = "github-credentials"

        DEPLOY_USER = "root"
        DEPLOY_HOST = "117.72.35.70"
        DEPLOY_PORT = "22"
        DEPLOY_SSH_ID = "server-ssh-credentials"
        // ========== 配置结束 ==========
        // 注意：GIT_COMMIT_SHORT / BUILD_TIMESTAMP / IMAGE_TAG 不可在这里通过 sh() 赋值（会导致 DSL 加载失败）
    }

    stages {
        stage('0. 初始化 & 显示参数') {
            steps {
                echo "========== 初始化 =========="
                echo "选择的服务：${params.SERVICE_NAME}"
                echo "仓库：${env.GITHUB_REPO}"
                echo "镜像仓库：${env.DOCKER_REGISTRY}/${env.DOCKER_NAMESPACE}"
                echo "部署主机：${env.DEPLOY_HOST}"
                echo "=========================="
            }
        }

        stage('1. 检出代码') {
            steps {
                echo "========== 从 Git 克隆代码 =========="
                git branch: 'master',
                    url: "${GITHUB_REPO}",
                    credentialsId: "${GITHUB_CREDENTIALS_ID}"

                script {
                    // 必须在 checkout 之后通过 script 设置运行时变量到 env
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.BUILD_TIMESTAMP = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_TIMESTAMP}-${env.GIT_COMMIT_SHORT}"

                    echo "分支：master"
                    echo "Commit：${env.GIT_COMMIT_SHORT}"
                    echo "构建时间：${env.BUILD_TIMESTAMP}"
                    echo "镜像标签：${env.IMAGE_TAG}"
                }
            }
        }

        stage('2. Maven构建') {
            steps {
                echo "========== Maven 构建 ${params.SERVICE_NAME} =========="
                sh """
                    echo "清理和编译 ${SERVICE_NAME}..."
                    mvn clean package -DskipTests -U -pl ${SERVICE_NAME} -am

                    echo "构建完成，检查 jar 包..."
                    ls -lh ${SERVICE_NAME}/target/ || true
                """
            }
        }

        stage('3. 构建 Docker 镜像') {
            steps {
                echo "========== 构建 Docker 镜像 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"

                    echo "构建镜像：${FULL_IMAGE_NAME}:${env.IMAGE_TAG}"

                    // 使用 Groovy 三引号字符串让 ${env.IMAGE_TAG} 在本地被替换后传给 shell
                    sh """
                        docker build -f ${params.SERVICE_NAME}/Dockerfile \\
                          --build-arg BUILD_TIME=${env.BUILD_TIMESTAMP} \\
                          --build-arg VCS_REF=${env.GIT_COMMIT_SHORT} \\
                          -t ${FULL_IMAGE_NAME}:${env.IMAGE_TAG} \\
                          -t ${FULL_IMAGE_NAME}:latest \\
                          .
                    """

                    echo "镜像构建完成"
                    sh "docker images | grep ${config.imageName} || true"
                }
            }
        }

        stage('4. 推送镜像到阿里云') {
            steps {
                echo "========== 推送镜像到阿里云 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"

                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh """
                            echo "登录到阿里云镜像仓库..."
                            echo "${DOCKER_PASS}" | docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} --password-stdin

                            echo "推送镜像：${env.IMAGE_TAG}"
                            docker push ${FULL_IMAGE_NAME}:${env.IMAGE_TAG}

                            echo "推送 latest 标签"
                            docker push ${FULL_IMAGE_NAME}:latest

                            docker logout ${DOCKER_REGISTRY} || true
                            echo "推送完成"
                        """
                    }
                }
            }
        }

        stage('5. 部署到服务器') {
            steps {
                echo "========== 部署到服务器 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"
                    def CONTAINER_NAME = config.containerName
                    def CONTAINER_PORT = config.containerPort
                    // 构造远端脚本（在本地插入变量值，然后通过 ssh 发送到远端执行）
                    def remoteDeployScript = """#!/bin/bash
set -e

FULL_IMAGE_NAME="${FULL_IMAGE_NAME}"
CONTAINER_NAME="${CONTAINER_NAME}"
CONTAINER_PORT="${CONTAINER_PORT}"
IMAGE_TAG="${env.IMAGE_TAG}"
DOCKER_REGISTRY="${DOCKER_REGISTRY}"
DOCKER_USER="${DOCKER_USER ?: ''}"
DOCKER_PASS="${DOCKER_PASS ?: ''}"

echo "========== 部署 ${params.SERVICE_NAME} =========="
echo "镜像：${FULL_IMAGE_NAME}:${env.IMAGE_TAG}"
echo "容器名：${CONTAINER_NAME}"
echo "容器端口：${CONTAINER_PORT}"

# 登录到镜像仓库（如果环境中有 DOCKER_PASS/DOCKER_USER，则尝试登录；在 Jenkins 上前一步已经登录过可根据需要保留）
if [ -n "${DOCKER_USER}" ] && [ -n "${DOCKER_PASS}" ]; then
  echo "${DOCKER_PASS}" | docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} --password-stdin || true
fi

# 拉取镜像
echo "拉取镜像..."
docker pull ${FULL_IMAGE_NAME}:${IMAGE_TAG} || true

# 停止旧容器
echo "停止旧容器..."
docker stop ${CONTAINER_NAME} || true

# 删除旧容器
echo "删除旧容器..."
docker rm ${CONTAINER_NAME} || true

# 启动新容器
echo "启动新容器..."
docker run -d \\
  --name ${CONTAINER_NAME} \\
  -p ${CONTAINER_PORT}:8080 \\
  --restart=always \\
  --health-cmd="curl -f http://localhost:8080/health || exit 1" \\
  --health-interval=30s \\
  --health-timeout=10s \\
  --health-retries=3 \\
  -e JAVA_OPTS="-Xms256m -Xmx512m" \\
  ${FULL_IMAGE_NAME}:${IMAGE_TAG}

# 等待并检查
sleep 10
docker ps | grep ${CONTAINER_NAME} || true

echo "查看日志（最近 20 行）..."
docker logs ${CONTAINER_NAME} | tail -20 || true

echo "部署完成！"

# 登出（可选）
docker logout ${DOCKER_REGISTRY} || true
"""

                    // 使用 sshagent 将密钥注入，再通过 ssh 执行远端脚本
                    sshagent([env.DEPLOY_SSH_ID]) {
                        // 将 remoteDeployScript 内容通过 stdin 传给远端 ssh 执行
                        sh """
                            echo "连接到 ${DEPLOY_HOST} 并部署 ${params.SERVICE_NAME}..."
                            ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} 'bash -s' <<'ENDSSH'
${remoteDeployScript}
ENDSSH
                        """
                    }
                }
            }
        }

        stage('6. 健康检查') {
            steps {
                echo "========== 执行健康检查 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def CONTAINER_NAME = config.containerName
                    def CONTAINER_PORT = config.containerPort

                    sshagent([env.DEPLOY_SSH_ID]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} <<'HEALTH'
CONTAINER_NAME="${CONTAINER_NAME}"
CONTAINER_PORT="${CONTAINER_PORT}"

echo "========== 远端健康检查 =========="
# 等待服务起来
sleep 5

if docker ps | grep ${CONTAINER_NAME}; then
  echo "✓ 容器运行中"
else
  echo "✗ 容器未运行"
  exit 1
fi

if netstat -tuln 2>/dev/null | grep :${CONTAINER_PORT}; then
  echo "✓ 端口 ${CONTAINER_PORT} 已开放"
fi

echo ""
echo "访问地址："
echo "  http://${DEPLOY_HOST}:${CONTAINER_PORT}"
echo ""
echo "健康检查完成 ✓"
HEALTH
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def config = getServiceConfig(params.SERVICE_NAME)
                echo "========== 构建部署成功 =========="
                echo "服务：${params.SERVICE_NAME}"
                echo "镜像：${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}:${env.IMAGE_TAG}"
                echo "服务器：${DEPLOY_USER}@${DEPLOY_HOST}"
                echo "容器：${config.containerName}（端口 ${config.containerPort}）"
            }
        }
        failure {
            script {
                def config = getServiceConfig(params.SERVICE_NAME)
                echo "========== 构建或部署失败（收集远端日志） =========="
                sshagent([env.DEPLOY_SSH_ID]) {
                    // 容错：即使 ssh 获取日志失败也不让 pipeline 再报错（|| true）
                    sh """
                        ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} <<'GETERR'
echo "失败容器日志："
docker logs ${config.containerName} 2>&1 | tail -50 || true
GETERR
                    """ || true
                }
            }
        }
        always {
            echo "========== 清理本地镜像 =========="
            sh '''
                docker images | grep xf-spring-cloud-alibaba | tail -n +4 | awk '{print $3}' | xargs -r docker rmi -f || true
            '''
        }
    }
}

// ========== 辅助函数 ==========
def getServiceConfig(serviceName) {
    def config = [:]

    switch(serviceName) {
        case 'cloud-consumer':
            config.containerName = 'cloud-consumer'
            config.containerPort = '9092'
            config.imageName = 'cloud-consumer'
            break
        case 'cloud-gateway':
            config.containerName = 'cloud-gateway'
            config.containerPort = '9090'
            config.imageName = 'cloud-gateway'
            break
        case 'cloud-producer':
            config.containerName = 'cloud-producer'
            config.containerPort = '9091'
            config.imageName = 'cloud-producer'
            break
        case 'cloud-user':
            config.containerName = 'cloud-user'
            config.containerPort = '9093'
            config.imageName = 'cloud-user'
            break
        default:
            error("未知的服务: ${serviceName}")
    }

    return config
}