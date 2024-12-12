## 基于spring cloud alibaba生态快速构建微服务脚手架

---
### 项目介绍

[![](https://img.shields.io/badge/-@remaindertime-FC5531?style=flat&logo=csdn&logoColor=FC5531&labelColor=424242)](https://blog.csdn.net/qq_39818325?type=blog)
![](https://img.shields.io/badge/jdk-17+-blue.svg)
![](https://img.shields.io/badge/springboot-3.3.5-{徽标颜色}.svg)
![](https://img.shields.io/badge/springcloud-2023.0.1-FF5500.svg)
![](https://img.shields.io/badge/springcloudalibaba-2023.0.1-FF6A00.svg)  
![](https://img.shields.io/badge/路由网关-gateway-FF4F8B.svg)
![](https://img.shields.io/badge/远程调用-openfeign-73DC8C.svg)
![](https://img.shields.io/badge/负载均衡-loadbalancer-8C4FFF.svg)
![](https://img.shields.io/badge/注册中心-nacos-red.svg)
![](https://img.shields.io/badge/配置中心-nacos-red.svg)
![](https://img.shields.io/badge/分布式事务-seata-33302E.svg)
![](https://img.shields.io/badge/限流降级-sentinel-red.svg)
![](https://img.shields.io/badge/消息队列-rocketmq-D77310.svg)
---
>这是一个基于 Spring Cloud Alibaba 生态的基础微服务脚手架，旨在快速构建现代化的微服务架构项目。该项目整合了 Spring Cloud Alibaba 的核心组件，并提供了开箱即用的最佳实践配置，为开发者节省繁琐的环境搭建时间，专注于业务逻辑的开发。
> 
### 集成组件
- 服务网关（gateway）：基于 Spring Cloud Gateway，支持动态路由和高效负载均衡。
- 远程调用（openfeign）：支持声明式的服务间调用，减少冗余代码，提高开发效率。
- 负载均衡（loadbalancer）：提供高性能负载均衡策略，保证服务高可用。
- 注册中心（nacos）：作为服务注册与配置中心，支持动态配置管理和服务发现。
- 配置中心（nacos）：集中管理多环境配置，支持热更新。
- 分布式事务（seata）：解决跨服务调用中的事务一致性问题。
- 限流降级（sentinel）：灵活控制流量，实现熔断与系统保护。
- 消息队列（rocketmq）：高性能分布式消息通信，支持异步处理与事件驱动。

### 项目优势
- 萌新友好：该脚手架对初学者尤为友好，代码结构清晰，注释完善，是学习和实践微服务及分布式系统的理想项目
- 高扩展性：支持动态扩展服务和功能模块，适用于小型项目起步及大型微服务项目的演进。
- 社区支持强大：基于 Spring Cloud Alibaba 的生态，提供丰富的文档和技术支持。
快速交付：内置的脚手架和最佳实践配置帮助团队减少项目启动时间，加速开发进程。

### 快速开始

你只需克隆本项目，并根据你的业务需求配置 Nacos 等相关组件环境，即可快速启动项目。如果你对微服务架构感兴趣或正在寻找高效的基础脚手架项目，这个项目将是你的理想选择。

---

欢迎点击 Star 支持这个项目，让更多开发者受益！ 😊