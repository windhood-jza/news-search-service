package com.news.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 新闻检索服务应用
 * 主应用入口类
 * 继承SpringBootServletInitializer以支持WAR部署在Tomcat中
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NewsServiceApplication extends SpringBootServletInitializer {
    
    /**
     * 配置SpringApplicationBuilder
     * 该方法支持WAR包部署到外部容器
     *
     * @param application 应用构建器
     * @return 配置后的应用构建器
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(NewsServiceApplication.class);
    }
    
    /**
     * 应用启动入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NewsServiceApplication.class, args);
    }
} 