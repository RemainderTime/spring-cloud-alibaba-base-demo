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
        // ========== ⬇️ 需要修改的配置 ⬇️ ==========
        DOCKER_REGISTRY = "registry.cn-hangzhou.aliyuncs.com"
        DOCKER_NAMESPACE = "xf-spring-cloud-alibaba"
        DOCKER_CREDENTIALS_ID = "aliyun-docker-credentials"

        GITHUB_REPO = "https://github.com/RemainderTime/spring-cloud-alibaba-base-demo.git"
        GITHUB_CREDENTIALS_ID = "github-credentials"

        DEPLOY_USER = "root"
        DEPLOY_HOST = "117.72.35.70"
        DEPLOY_PORT = "22"
        DEPLOY_SSH_ID = "server-ssh-credentials"
        // ========== ⬆️ 上面这些需要改 ⬆️ ==========

        // 自动生成
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        BUILD_TIMESTAMP = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
        IMAGE_TAG = "${BUILD_TIMESTAMP}-${GIT_COMMIT_SHORT}"

        // 根据选择的服务动态设置配置
        SERVICE_CONFIG = getServiceConfig(params.SERVICE_NAME)
    }

    stages {
        stage('0. 显示构建信息') {
            steps {
                echo "========== 构建信息 =========="
                echo "选择的服务：${params.SERVICE_NAME}"
                echo "容器名：${SERVICE_CONFIG.containerName}"
                echo "容器端口：${SERVICE_CONFIG.containerPort}"
                echo "镜像名：${SERVICE_CONFIG.imageName}"
                echo "=========================="
            }
        }

        stage('1. 检出代码') {
            steps {
                echo "========== 从GitHub克隆代码 =========="
                git branch: 'master',
                    url: "${GITHUB_REPO}",
                    credentialsId: "${GITHUB_CREDENTIALS_ID}"

                script {
                    echo "仓库：${GITHUB_REPO}"
                    echo "分支：master"
                    echo "Commit：${GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('2. Maven构建') {
            steps {
                echo "========== 构建 ${params.SERVICE_NAME} =========="
                sh '''
                    echo "清理和编译 ${SERVICE_NAME}..."
                    mvn clean package -DskipTests -U -pl ${SERVICE_NAME} -am

                    echo "构建完成，检查jar包..."
                    ls -lh ${SERVICE_NAME}/target/
                '''
            }
        }

        stage('3. 构建Docker镜像') {
            steps {
                echo "========== 构建Docker镜像 =========="
                sh '''
                    FULL_IMAGE_NAME="${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${SERVICE_CONFIG[imageName]}"

                    echo "构建镜像：${FULL_IMAGE_NAME}:${IMAGE_TAG}"

                    docker build -f ${SERVICE_NAME}/Dockerfile \
                      --build-arg BUILD_TIME=${BUILD_TIMESTAMP} \
                      --build-arg VCS_REF=${GIT_COMMIT_SHORT} \
                      -t ${FULL_IMAGE_NAME}:${IMAGE_TAG} \
                      -t ${FULL_IMAGE_NAME}:latest \
                      .

                    echo "镜像构建完成"
                    docker images | grep ${SERVICE_CONFIG[imageName]}
                '''
            }
        }

        stage('4. 推送镜像到阿里云') {
            steps {
                echo "========== 推送镜像到阿里云 =========="
                script {
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            FULL_IMAGE_NAME="${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${SERVICE_CONFIG[imageName]}"

                            echo "登录到阿里云镜像仓库..."
                            echo ${DOCKER_PASS} | docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} --password-stdin

                            echo "推送镜像：${IMAGE_TAG}"
                            docker push ${FULL_IMAGE_NAME}:${IMAGE_TAG}

                            echo "推送latest标签"
                            docker push ${FULL_IMAGE_NAME}:latest

                            docker logout ${DOCKER_REGISTRY}
                            echo "推送完成"
                        '''
                    }
                }
            }
        }

        stage('5. 部署到服务器') {
            steps {
                echo "========== 部署到服务器 =========="
                sshagent(["${DEPLOY_SSH_ID}"]) {
                    sh '''
                        echo "连接到服务器并部署 ${SERVICE_NAME}..."

                        ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} << 'DEPLOY_SCRIPT'
                            set -e

                            FULL_IMAGE_NAME="${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${SERVICE_CONFIG[imageName]}"
                            CONTAINER_NAME="${SERVICE_CONFIG[containerName]}"
                            CONTAINER_PORT="${SERVICE_CONFIG[containerPort]}"

                            echo "========== 部署 ${SERVICE_NAME} =========="
                            echo "镜像：${FULL_IMAGE_NAME}:${IMAGE_TAG}"
                            echo "容器名：${CONTAINER_NAME}"
                            echo "容器端口：${CONTAINER_PORT}"

                            # 登录到阿里云
                            echo "登录到阿里云镜像仓库..."
                            echo ${DOCKER_PASS} | docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} --password-stdin

                            # 拉取最新镜像
                            echo "拉取镜像..."
                            docker pull ${FULL_IMAGE_NAME}:${IMAGE_TAG}

                            # 停止旧容器
                            echo "停止旧容器..."
                            docker stop ${CONTAINER_NAME} || true

                            # 删除旧容器
                            echo "删除旧容器..."
                            docker rm ${CONTAINER_NAME} || true

                            # 启动新容器
                            echo "启动新容器..."
                            docker run -d \
                              --name ${CONTAINER_NAME} \
                              -p ${CONTAINER_PORT}:8080 \
                              --restart=always \
                              --health-cmd="curl -f http://localhost:8080/health || exit 1" \
                              --health-interval=30s \
                              --health-timeout=10s \
                              --health-retries=3 \
                              -e JAVA_OPTS="-Xms512m -Xmx1024m" \
                              ${FULL_IMAGE_NAME}:${IMAGE_TAG}

                            # 等待容器启动
                            echo "等待容器启动..."
                            sleep 10

                            # 检查容器状态
                            echo "检查容器状态..."
                            docker ps | grep ${CONTAINER_NAME}

                            # 查看日志
                            echo "查看应用日志..."
                            docker logs ${CONTAINER_NAME} | tail -20

                            echo "部署完成！"

                            # 登出
                            docker logout ${DOCKER_REGISTRY}
DEPLOY_SCRIPT
                    '''
                }
            }
        }

        stage('6. 健康检查') {
            steps {
                echo "========== 执行健康检查 =========="
                sshagent(["${DEPLOY_SSH_ID}"]) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} << 'HEALTH_CHECK'
                            CONTAINER_NAME="${SERVICE_CONFIG[containerName]}"
                            CONTAINER_PORT="${SERVICE_CONFIG[containerPort]}"

                            echo "========== 健康检查 =========="
                            echo "服务：${SERVICE_NAME}"
                            echo "容器：${CONTAINER_NAME}"
                            echo "端口：${CONTAINER_PORT}"

                            sleep 5

                            # 检查容器是否运行
                            if docker ps | grep ${CONTAINER_NAME}; then
                                echo "✓ 容器运行中"
                            else
                                echo "✗ 容器未运行"
                                exit 1
                            fi

                            # 检查端口是否开放
                            if netstat -tuln 2>/dev/null | grep :${CONTAINER_PORT}; then
                                echo "✓ 端口${CONTAINER_PORT}已开放"
                            fi

                            echo ""
                            echo "访问地址："
                            echo "  http://your-server-ip:${CONTAINER_PORT}"
                            echo ""
                            echo "健康检查完成 ✓"
HEALTH_CHECK
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "========== 构建部署成功 =========="
            echo "服务：${params.SERVICE_NAME}"
            echo "镜像：${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${SERVICE_CONFIG.imageName}:${IMAGE_TAG}"
            echo "服务器：${DEPLOY_USER}@${DEPLOY_HOST}"
            echo "容器：${SERVICE_CONFIG.containerName}（端口${SERVICE_CONFIG.containerPort}）"
        }
        failure {
            echo "========== 构建或部署失败 =========="
            script {
                sshagent(["${DEPLOY_SSH_ID}"]) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} << 'ERROR_LOG'
                            echo "失败容器日志："
                            docker logs ${SERVICE_CONFIG[containerName]} 2>&1 | tail -50 || true
ERROR_LOG
                    ''' || true
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