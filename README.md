# yren-java-project-general-study

## 项目简介

这是一个Java学习项目的复合工程，采用Maven多模块架构。主工程负责统一的依赖版本管理，各个子模块专注于不同技术领域的学习和实践。

## 项目结构

```
yren-java-project-general-study/
├── pom.xml                           # 主工程POM，负责依赖版本管理
├── doc/                              # 项目文档目录
│   └── DynamicTp & Nacos Config 动态线程池监控.md
├── cloud-dynamictp-nacos-project/    # 动态线程池学习项目
│   ├── pom.xml
└───└── src/
```

## 技术栈版本

### 核心框架
- **JDK**: 8
- **Spring Boot**: 2.7.18
- **Spring Cloud**: 2021.0.3
- **Spring Cloud Alibaba**: 2021.0.1.0

### 专项技术
- **Dynamic TP**: 1.2.2 (动态线程池监控)

## 子项目介绍

### 1. cloud-dynamictp-nacos-project

**学习目标**: 动态线程池监控与管理

**技术要点**:
- 集成DynamicTp框架实现线程池动态配置
- 使用Nacos作为配置中心，支持线程池参数热更新
- 集成Spring Boot Actuator暴露监控端点
- 实现线程池运行状态的实时监控

**核心依赖**:
- `dynamic-tp-spring`: 动态线程池核心库
- `dynamic-tp-spring-cloud-starter-nacos`: Nacos配置中心集成
- `spring-cloud-starter-alibaba-nacos-config`: Nacos配置客户端
- `spring-boot-starter-actuator`: 监控端点

## 快速开始

### 环境要求
- JDK 8+
- Maven 3.6+
- Nacos Server (用于动态线程池项目)

### 构建项目

```bash
# 克隆项目
git clone <repository-url>
cd yren-java-project-general-study

# 编译整个项目
mvn clean compile

# 打包所有模块
mvn clean package
```

### 运行子项目

```bash
# 进入动态线程池项目目录
cd cloud-dynamictp-nacos-project

# 启动应用
mvn spring-boot:run
```

## 学习资源

### 官方文档
- [DynamicTp官方文档](https://dynamictp.cn/)
- [Spring Cloud Alibaba文档](https://spring-cloud-alibaba-group.github.io/github-pages/2021/zh-cn/index.html)
- [Nacos官方文档](https://nacos.io/zh-cn/docs/quick-start.html)

### 参考资料
- [Java线程池实现原理及其在美团业务中的实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)
- 项目文档：`doc/DynamicTp & Nacos Config 动态线程池监控.md`

## 项目特点

1. **统一版本管理**: 主POM统一管理所有依赖版本，确保子模块间的兼容性
2. **模块化设计**: 每个子项目专注特定技术领域，便于独立学习和实践
3. **实践导向**: 结合实际业务场景，不仅学习理论还注重实践应用
4. **文档完善**: 提供详细的学习文档和最佳实践指南

## 贡献指南

欢迎提交Issue和Pull Request来完善这个学习项目！

## 许可证

本项目仅用于学习目的。