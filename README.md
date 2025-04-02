# 新闻检索服务

这是一个基于Spring Boot的新闻内容检索系统，提供新闻内容的全文检索功能。

## 功能特点

- 支持复杂的关键词搜索（AND/OR逻辑组合）
- 智能权重排序
- 关键词高亮显示
- XML内容解析
- JSON格式输出
- 分页查询

## 技术栈

- Java 8
- Spring Boot 2.7.12
- MySQL 8.0
- Thymeleaf
- HikariCP
- Maven

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 配置数据库

1. 创建数据库
2. 修改`application.properties`中的数据库连接信息

### 构建和运行

```bash
mvn clean package
java -jar target/news.war
```

### 访问服务

启动后访问：http://localhost:8080

## API文档

详细的API文档请参考 [api_spec.md](api_spec.md)

## 许可证

MIT License