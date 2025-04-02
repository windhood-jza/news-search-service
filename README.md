# 新闻检索服务

新闻内容检索服务是一个基于Spring Boot开发的Web应用，用于搜索和检索新闻文章内容。系统支持全文检索、关键词高亮和相关度排序等功能。

## 功能特点

- 全文检索：支持在新闻标题和内容中搜索关键词
- 关键词高亮：自动高亮显示搜索结果中的关键词
- 智能排序：根据相关度对搜索结果进行排序
- JSON输出：支持JSON格式的API输出
- 灵活配置：支持在线配置数据库连接

## 技术栈

- **后端：**Spring Boot 2.7.x
- **前端：**Thymeleaf + Bootstrap 5 + jQuery
- **数据库：**支持MySQL、Oracle、PostgreSQL、SQL Server
- **构建工具：**Maven

## 系统要求

- JDK 1.8+
- Maven 3.5+
- 支持的数据库（MySQL/Oracle/PostgreSQL/SQL Server）

## 快速开始

### 编译和打包

```bash
# 克隆项目
git clone https://github.com/windhood-jza/news-search-service.git
cd news-search-service

# 编译打包
mvn clean package
```

### 运行服务

```bash
java -jar target/news.war
```

默认情况下，服务将在 http://localhost:8080 启动。

### 部署到Tomcat

将生成的`target/news.war`文件部署到Tomcat的webapps目录即可。

## 配置数据库

1. 访问系统的配置页面（http://localhost:8080/news/config-page）
2. 勾选“启用数据库”
3. 选择数据库类型，并填写相关连接信息
4. 点击“测试连接”确保连接正常
5. 点击“保存配置”并重启服务

## 使用方法

### 基本搜索

1. 访问搜索页面（http://localhost:8080/news/search-page）
2. 在搜索框中输入关键词
3. 点击搜索按钮执行搜索

### 高级搜索

- **AND搜索：**用空格分隔多个关键词，如“经济 政策”
- **OR搜索：**用OR分隔多个关键词，如“经济 OR 政策”

### API访问

搜索API端点：`POST /news/search/json`

请求示例：

```json
{
  "keywords": "经济",
  "page": 0,
  "size": 10,
  "sortField": "score",
  "sortDirection": "desc"
}
```

## 配置文件

配置文件位于`config/application.properties`。主要配置项包括：

```properties
# 服务端口
server.port=8080

# 数据库配置
spring.datasource.enabled=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/news
spring.datasource.username=root
spring.datasource.password=password
```

## 问题反馈

如有问题或建议，请提交Issue或参与讨论。

## 许可证

MIT
