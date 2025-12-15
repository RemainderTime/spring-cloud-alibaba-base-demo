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

                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.BUILD_TIMESTAMP = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_TIMESTAMP}-${env.GIT_COMMIT_SHORT}"
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
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.BUILD_TIMESTAMP  = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
                    env.IMAGE_TAG        = "${env.BUILD_TIMESTAMP}-${env.GIT_COMMIT_SHORT}"
                    echo "当前 Commit：${env.GIT_COMMIT_SHORT}"
                    echo "镜像 Tag：${env.IMAGE_TAG}"
                }
            }
        }

        stage('2. Maven构建') {
            steps {
                echo "========== 构建 ${params.SERVICE_NAME} =========="
                sh '''
                    echo "清理和编译 ${SERVICE_NAME}..."
                    mvn clean package -DskipTests -pl ${SERVICE_NAME} -am

                    echo "构建完成，检查jar包..."
                    ls -lh ${SERVICE_NAME}/target/
                '''
            }
        }

        stage('3. 构建Docker镜像') {
            steps {
                echo "========== 构建Docker镜像 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"

                    echo "构建镜像：${FULL_IMAGE_NAME}:${IMAGE_TAG}"

                    sh """
                        docker build \
                          --build-arg SERVICE_NAME=${params.SERVICE_NAME} \
                          --build-arg BUILD_TIME=${BUILD_TIMESTAMP} \
                          --build-arg VCS_REF=${GIT_COMMIT_SHORT} \
                          -t ${FULL_IMAGE_NAME}:${IMAGE_TAG} \
                          -t ${FULL_IMAGE_NAME}:latest \
                          .
                    """

                    echo "镜像构建完成"
                    sh "docker images | grep ${config.imageName}"
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
                        sh '''
                            echo "登录到阿里云镜像仓库..."
                            docker login -u ${DOCKER_USER} -p ${DOCKER_PASS} ${DOCKER_REGISTRY}

                            echo "推送镜像：${IMAGE_TAG}"
                            docker push ''' + FULL_IMAGE_NAME + ''':${IMAGE_TAG}

                            echo "推送latest标签"
                            docker push ''' + FULL_IMAGE_NAME + ''':latest

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
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def FULL_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}"
                    def IMAGE_TAG_VAR = env.IMAGE_TAG
                    def CONTAINER_NAME_VAR = config.containerName
                    def CONTAINER_PORT_VAR = config.containerPort

                    sshagent(["${DEPLOY_SSH_ID}"]) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} << 'DEPLOY_SCRIPT'
                                set -e

                                FULL_IMAGE_NAME="''' + FULL_IMAGE_NAME + '''"
                                CONTAINER_NAME="''' + CONTAINER_NAME_VAR + '''"
                                CONTAINER_PORT="''' + CONTAINER_PORT_VAR + '''"
                                IMAGE_TAG="''' + IMAGE_TAG_VAR + '''"

                                echo "========== 部署 ''' + params.SERVICE_NAME + ''' =========="
                                echo "镜像：${FULL_IMAGE_NAME}:${IMAGE_TAG}"
                                echo "容器名：${CONTAINER_NAME}"
                                echo "容器端口：${CONTAINER_PORT}"

                                # 删除旧镜像（只保留最新1个版本）
                                echo "清理旧镜像..."
                                docker images ${FULL_IMAGE_NAME} --format "table {{.ID}}\t{{.CreatedAt}}" | tail -n +2 | awk '{print $1}' | xargs -r docker rmi -f || true

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
                                docker run -d \\
                                  --name ${CONTAINER_NAME} \\
                                  -p ${CONTAINER_PORT}:8080 \\
                                  --restart=always \\
                                  -m 512m \\
                                  --memory-swap 512m \\
                                  --memory-reservation 800m \\
                                  -e JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \\
                                  -e NACOS_SERVER_ADDR=117.72.35.70 \\
                                  -e NACOS_USERNAME=nacos \\
                                  -e NACOS_PWD=love..520 \\
                                  ${FULL_IMAGE_NAME}:${IMAGE_TAG}

                                # 等待容器启动
                                echo "等待容器启动..."
                                sleep 15

                                # 检查容器状态
                                echo "检查容器状态..."
                                if docker ps | grep ${CONTAINER_NAME}; then
                                    echo "✓ 容器运行中"
                                else
                                    echo "✗ 容器未运行"
                                    docker logs ${CONTAINER_NAME} 2>&1 | tail -50 || true
                                fi

                                echo "部署完成！"
DEPLOY_SCRIPT
                        '''
                    }
                }
            }
        }

        stage('6. 健康检查') {
            steps {
                echo "========== 执行健康检查 =========="
                script {
                    def config = getServiceConfig(params.SERVICE_NAME)
                    def CONTAINER_NAME_VAR = config.containerName
                    def CONTAINER_PORT_VAR = config.containerPort

                    sshagent(["${DEPLOY_SSH_ID}"]) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no -p ${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST} << 'HEALTH_CHECK'
                                CONTAINER_NAME="''' + CONTAINER_NAME_VAR + '''"
                                CONTAINER_PORT="''' + CONTAINER_PORT_VAR + '''"

                                echo "========== 健康检查 =========="
                                echo "服务：''' + params.SERVICE_NAME + '''"
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
                                echo "  http://117.72.35.70:${CONTAINER_PORT}"
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
                echo "镜像：${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${config.imageName}:${IMAGE_TAG}"
                echo "服务器：${DEPLOY_USER}@${DEPLOY_HOST}"
                echo "容器：${config.containerName}（端口${config.containerPort}）"
            }
        }
        failure {
            echo "========== 构建或部署失败 =========="
        }
        always {
            echo "========== 清理本地镜像 =========="
            sh script: '''
                docker images | grep xf-spring-cloud-alibaba | tail -n +4 | awk '{print $3}' | xargs -r docker rmi -f || true
            ''', returnStatus: true
        }
    }
}

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