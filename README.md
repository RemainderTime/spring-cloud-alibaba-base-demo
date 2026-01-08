# Spring Cloud Alibaba Microservices Scaffold

[![](https://img.shields.io/badge/Author-RemainderTime-FC5531?style=flat&logo=csdn&logoColor=FC5531&labelColor=424242)](https://blog.csdn.net/qq_39818325?type=blog)
![](https://img.shields.io/badge/JDK-17+-blue.svg?logo=java&logoColor=white)
![](https://img.shields.io/badge/SpringBoot-3.3.5-brightgreen.svg?logo=springboot&logoColor=white)
![](https://img.shields.io/badge/SpringCloud-2023.0.1-FF5500.svg?logo=spring&logoColor=white)
![](https://img.shields.io/badge/SCA-2023.0.1-FF6A00.svg?logo=alibaba&logoColor=white)
![](https://img.shields.io/badge/Gateway-Enabled-FF4F8B.svg)
![](https://img.shields.io/badge/Nacos-Registry%20%26%20Config-blue.svg)
![](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

---

## 📖 项目介绍 | Introduction

本项目是一个基于 **Spring Cloud Alibaba** 生态的**企业级微服务脚手架**，旨在帮助开发者快速构建现代化、高可用的分布式系统。项目深度整合了 Spring Cloud Alibaba 的核心组件（Nacos, Sentinel, Seata, RocketMQ 等），并提供了开箱即用的最佳实践配置。

**核心目标：**
- **快速启动**：节省繁琐的环境搭建时间，让开发者专注于业务逻辑。
- **规范标准**：提供标准的微服务分层架构和配置模板。
- **易于扩展**：模块化设计，支持从单体平滑过渡到微服务架构。

### 🏗 核心架构 (Architecture)
- **cloud-gateway**: API 网关，负责统一接入、动态路由、鉴权和流量控制。
- **cloud-common**: 公共模块，包含全局异常处理、通用工具类、统一响应实体等。
- **cloud-user**: 用户服务示例，提供基础的用户管理能力。
- **cloud-producer**: 服务提供者示例，演示核心业务逻辑。
- **cloud-consumer**: 服务消费者示例，演示 OpenFeign 远程调用。

---

## 🛠 技术栈 | Tech Stack

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| **Spring Boot** | 3.3.5 | 核心框架 |
| **Spring Cloud** | 2023.0.1 | 微服务标准 |
| **Spring Cloud Alibaba** | 2023.0.1 | 阿里微服务全家桶 |
| **Nacos** | 2.x | 注册中心 & 配置中心 |
| **Spring Cloud Gateway** | 3.x | API 网关 |
| **OpenFeign** | Recent | 声明式 HTTP 客户端 |
| **Sentinel** | Recent | 流量防卫兵（限流、熔断） |
| **Seata** | Recent | 分布式事务解决方案 |
| **RocketMQ** | Recent | 分布式消息队列 |

---

## � 配套教程 | Tutorials

本系列教程共 **10 篇**，详细记录了从零构建本项目的全过程，涵盖了从环境搭建、核心组件集成到生产级部署的各个环节。

**专栏地址**：[🔥 Spring Cloud Alibaba 2023.x 实战手记](https://blog.csdn.net/qq_39818325/category_12857283.html)

### 📌 目录 (Syllabus)
1.  [（一）Spring Cloud Alibaba 2023.x：快速构建微服务项目](https://blog.csdn.net/qq_39818325/article/details/144500734)
2.  [（二）Spring Cloud Alibaba 2023.x：轻松集成 Nacos 注册与配置中心](https://blog.csdn.net/qq_39818325/article/details/144502345)
3.  [（三）Spring Cloud Alibaba 2023.x：OpenFeign 实现高效远程调用](https://blog.csdn.net/qq_39818325/article/details/144502729)
4.  [（四）Spring Cloud Alibaba 2023.x：高效构建 Gateway 网关服务](https://blog.csdn.net/qq_39818325/article/details/144502903)
5.  [（五）Spring Cloud Alibaba 2023.x：Seata 分布式事务配置与实现](https://blog.csdn.net/qq_39818325/article/details/144512122)
6.  [（六）Spring Cloud Alibaba 2023.x：Sentinel 流量控制与熔断限流实现](https://blog.csdn.net/qq_39818325/article/details/144534043)
7.  [（七）Spring Cloud Alibaba 2023.x：RocketMQ 消息队列配置与实现](https://blog.csdn.net/qq_39818325/article/details/144563799)
8.  [（八）Spring Cloud Alibaba 2023.x：网关统一鉴权与登录实现](https://blog.csdn.net/qq_39818325/article/details/151293516)
9.  [（九）Spring Cloud Alibaba 2023.x：微服务接口文档统一管理与聚合](https://blog.csdn.net/qq_39818325/article/details/151577348)
10. [（十）Spring Cloud Alibaba 2023.x：生产级 CI/CD 全链路实战（从 Dockerfile 到 Jenkins）](https://blog.csdn.net/qq_39818325/category_12857283.html)

---

## �🚀 快速开始 | Quick Start

### 1. 环境准备 (Prerequisites)
确保本地环境已安装并运行以下服务：
- **JDK 17+**
- **Maven 3.8+**
- **Nacos Server 2.x** (必须运行)
- **Redis** & **MySQL** (根据业务需要)

### 2. Nacos 配置 (Configuration) - **IMPORTANT**
本项目完全基于 Nacos 进行配置管理。请按照以下步骤操作：

#### 2.1 创建命名空间 (Namespace)
登录 Nacos 控制台（通常在左侧菜单“命名空间” -> “新建命名空间”）。
> **注意**：Nacos 允许自定义命名空间 ID（"命名空间ID"栏非必填，不填则自动生成）。

*   **方案 A（推荐 - 最省事）**：
    在创建时，**手动填入** ID：`74193cd9-fac4-4f2a-addc-47c60508b15c`。
    这样您无需修改任何代码配置即可直接启动。
    
*   **方案 B（自定义）**：
    如果您使用了 Nacos 自动生成的 ID 或自定义了其他 ID，请务必修改本项目所有模块（`cloud-gateway`, `cloud-user` 等）下的 `src/main/resources/application.yml` 文件，将 `namespace` 字段替换为您实际的 ID。

#### 2.2 导入配置文件
将项目根目录下的 **`doc/`** 文件夹中的 YAML 配置文件导入到 Nacos 的配置中心（在上述 Namespace 下）：
- `common-config.yaml`: 通用配置
- `redis-common.yaml`: Redis 连接配置
- `seata-common.yaml`: Seata 事务配置
- `rocketmq-common.yaml`: MQ 配置

#### 2.3 环境变量 (Environment Variables)
项目支持通过环境变量动态设置 Nacos 地址，方便容器化部署。默认值为 `127.0.0.1:8848`。
可以在 IDEA 启动配置或 `docker-compose` 中设置：
- `NACOS_SERVER_ADDR`: Nacos 服务地址 (e.g., `192.168.1.100:8848`)
- `NACOS_USERNAME`: Nacos 用户名 (Default: `nacos`)
- `NACOS_PWD`: Nacos 密码 (Default: `nacos`)

### 3. 启动项目 (Run)
1. **启动网关**: 运行 `CloudGatewayApplication`。
2. **启动服务**: 依次运行 `CloudUserApplication`、`CloudProducerApplication` 等核心服务。
3. **验证**: 访问 Nacos 控制台查看服务是否注册成功。

---

## 📊 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=RemainderTime/spring-cloud-alibaba-base-demo&type=Date)](https://star-history.com/#RemainderTime/spring-cloud-alibaba-base-demo&Date)

---

## 🤝 贡献与支持
如果觉得这个项目对你有帮助，请给个 **Star** ⭐️ 支持一下！你的支持是我更新的动力！
如有问题，欢迎提交 Issue 或 PR。

License: [Apache 2.0](LICENSE)
