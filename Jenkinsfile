pipeline {
    agent any

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

    // 环境变量
    environment {
        DOCKER_REGISTRY = "crpi-rq074obigx0czrju.cn-chengdu.personal.cr.aliyuncs.com"
        DOCKER_NAMESPACE = "xf-spring-cloud-alibaba"
        DOCKER_CREDENTIALS_ID = "aliyun-docker-credentials"
        GITHUB_REPO = "git@github.com:RemainderTime/spring-cloud-alibaba-base-demo.git"
        GITHUB_CREDENTIALS_ID = "github-ssh-key"
        DEPLOY_USER = "root"
        DEPLOY_HOST = "117.72.35.70"
        DEPLOY_PORT = "22"
        DEPLOY_SSH_ID = "server-ssh-credentials"
        // Maven 编译优化参数已假定在 docker-compose.yaml 中全局设置
    }

    stages {
        stage('0. 显示构建信息') {
            steps {
                echo "========== 构建信息 =========="
                echo "选择的服务：${params.SERVICE_NAME}"
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    echo "容器名：${config.containerName}"
                    echo "容器端口：${config.containerPort}"
                    echo "镜像名：${config.imageName}"
                    // ⚠️ Stage 0 避免运行任何 sh 命令
                }
                echo "=========================="
            }
        }

        stage('1. 检出代码') {
            steps {
                echo "========== 从 GitHub 拉取代码 =========="
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: 'master']],
                    userRemoteConfigs: [[
                        url: env.GITHUB_REPO,
                        credentialsId: env.GITHUB_CREDENTIALS_ID
                    ]]
                ])
                script {
                    // 🟢 修正 1：确保所有环境变量在检出代码后设置
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.BUILD_TIMESTAMP = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_TIMESTAMP}-${env.GIT_COMMIT_SHORT}"
                    echo "当前 Commit：${env.GIT_COMMIT_SHORT}"
                    echo "镜像 Tag：${env.IMAGE_TAG}"
                }
            }
        }

        stage('1.5. Maven 编译') {
            steps {
                echo "========== Maven 编译 (速度优化：mvn install) =========="
                script {
                    // 🟢 优化 2：使用 mvn install 确保依赖被缓存，且使用 --fail-at-end
                    sh "mvn install -DskipTests --fail-at-end -pl ${params.SERVICE_NAME} -am -Dmaven.repo.local=/root/.m2/repository"
                }
            }
        }

        stage('2. 构建Docker镜像') {
            steps {
                echo "========== 构建Docker镜像 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"

                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                       // 🟢 修正：使用 echo + docker login 的标准 Shell 方式，避免 Groovy 插值警告
                        sh '''
                            echo "${DOCKER_PASS}" | docker login -u ${DOCKER_USER} --password-stdin ${DOCKER_REGISTRY}
                        '''
                    }

                    sh """
                        docker build \\
                          --build-arg SERVICE_NAME=${params.SERVICE_NAME} \\
                          --build-arg BUILD_TIME=${env.BUILD_TIMESTAMP} \\
                          --build-arg VCS_REF=${env.GIT_COMMIT_SHORT} \\
                          -t ${FULL_IMAGE_NAME}:${env.IMAGE_TAG} \\
                          -t ${FULL_IMAGE_NAME}:latest \\
                          .
                    """
                    echo "镜像构建完成"
                }
            }
        }

        stage('3. 推送镜像到阿里云') {
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
                        // 🟢 修正 4：使用单行 Shell 命令，避免多行引号和转义问题
                        sh "echo '推送镜像：${IMAGE_TAG}'; docker push ${FULL_IMAGE_NAME}:${IMAGE_TAG}; echo '推送 latest 标签'; docker push ${FULL_IMAGE_NAME}:latest; echo '推送完成'"
                    }
                }
            }
        }

        stage('4. 部署到服务器') {
            steps {
                echo "========== 部署到服务器 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"
                    def IMAGE_TAG_VAR = env.IMAGE_TAG
                    def CONTAINER_NAME_VAR = config.containerName
                    def CONTAINER_PORT_VAR = config.containerPort

                    sshagent(["${DEPLOY_SSH_ID}"]) {
                        // 🟢 修正 5：将 Groovy 变量与 Shell 脚本拼接，并在内部转义 Shell 变量
                        sh '''
                            ssh -o StrictHostKeyChecking=no -p ''' + DEPLOY_PORT + ' ' + DEPLOY_USER + '@' + DEPLOY_HOST + ''' << 'DEPLOY_SCRIPT'
                                # 部署脚本开始
                                set -e
                                FULL_IMAGE_NAME="''' + FULL_IMAGE_NAME + '''"
                                CONTAINER_NAME="''' + CONTAINER_NAME_VAR + '''"
                                CONTAINER_PORT="''' + CONTAINER_PORT_VAR + '''"
                                IMAGE_TAG="''' + IMAGE_TAG_VAR + '''"
                                echo "========== 部署 ''' + params.SERVICE_NAME + ''' =========="
                                echo "镜像：\${FULL_IMAGE_NAME}:\${IMAGE_TAG}"
                                echo "容器名：\${CONTAINER_NAME}"
                                echo "容器端口：\${CONTAINER_PORT}"
                                docker pull \${FULL_IMAGE_NAME}:\${IMAGE_TAG}
                                docker stop \${CONTAINER_NAME} || true
                                docker rm \${CONTAINER_NAME} || true

                                # 🟢 修正 6：保留部署服务器上的旧镜像清理逻辑，并转义 awk 的 $1
                                docker images \${FULL_IMAGE_NAME} --format "table {{.ID}}\t{{.CreatedAt}}\t{{.Tag}}" | tail -n +4 | awk '{print \$1}' | xargs -r docker rmi -f || true

                                docker run -d \\
                                  --name \${CONTAINER_NAME} \\
                                  -p \${CONTAINER_PORT}:8080 \\
                                  --restart=always \\
                                  -m 512m \\
                                  --memory-swap 512m \\
                                  -e JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \\
                                  -e NACOS_SERVER_ADDR="改为自己的nacos地址" \\
                                  -e NACOS_USERNAME="改为自己的nacos账号" \\
                                  -e NACOS_PWD="改为自己的nacos密码" \\
                                  \${FULL_IMAGE_NAME}:\${IMAGE_TAG}
                                sleep 15
                                if docker ps | grep \${CONTAINER_NAME}; then
                                    echo "✓ 容器运行中"
                                else
                                    echo "✗ 容器未运行"
                                    docker logs \${CONTAINER_NAME} 2>&1 | tail -50 || true
                                    exit 1 # 部署失败，强制退出
                                fi
                                echo "部署完成！"
DEPLOY_SCRIPT
                        '''
                    }
                }
            }
        }

        stage('5. 健康检查') {
            steps {
                echo "========== 执行健康检查 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def CONTAINER_NAME_VAR = config.containerName
                    def CONTAINER_PORT_VAR = config.containerPort

                    sshagent(["${DEPLOY_SSH_ID}"]) {
                        // 🟢 修正 7：转义 SSH 脚本内部的 Shell 变量
                        sh '''
                            ssh -o StrictHostKeyChecking=no -p ''' + DEPLOY_PORT + ' ' + DEPLOY_USER + '@' + DEPLOY_HOST + ''' << 'HEALTH_CHECK'
                                CONTAINER_NAME="''' + CONTAINER_NAME_VAR + '''"
                                CONTAINER_PORT="''' + CONTAINER_PORT_VAR + '''"
                                echo "========== 健康检查 =========="
                                echo "服务：''' + params.SERVICE_NAME + '''"
                                echo "容器：\${CONTAINER_NAME}"
                                echo "端口：\${CONTAINER_PORT}"
                                sleep 5
                                if docker ps | grep \${CONTAINER_NAME}; then
                                    echo "✓ 容器运行中"
                                else
                                    echo "✗ 容器未运行"
                                    exit 1 # 健康检查失败，强制退出
                                fi
                                # 🟢 优化 8：检查 netstat 是否存在，并进行端口检查
                                if command -v netstat >/dev/null && netstat -tuln 2>/dev/null | grep :\${CONTAINER_PORT}; then
                                    echo "✓ 端口\${CONTAINER_PORT}已开放"
                                fi
                                echo ""
                                echo "访问地址： http://''' + DEPLOY_HOST + ''':\${CONTAINER_PORT}"
                                echo ""
                                echo "健康检查完成 ✓"
HEALTH_CHECK
                        '''
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
                echo "容器：${config.containerName}（端口${config.containerPort}）"
            }
        }
        failure {
            echo "========== 构建或部署失败 =========="
        }
        always {
            echo "========== 清理本地旧镜像和构建缓存 (最安全模式) =========="
            // 🟢 修正 9：使用最安全的 prune 命令，彻底避免与 Jenkins 容器冲突
            sh '''
               docker image prune -f || true
               docker builder prune -f || true
            '''
        }
    }
}

// 辅助函数
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