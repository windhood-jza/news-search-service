package com.news.service.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 应用配置类
 * 支持外部配置文件和内部配置
 */
@Slf4j
@Configuration
@PropertySources({
    @PropertySource(value = "file:${app.config.file}", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:application.properties")
})
public class AppConfig {

    @Value("${spring.datasource.enabled:false}")
    private boolean dataSourceEnabled;
    
    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;
    
    @Value("${spring.datasource.url:}")
    private String url;
    
    @Value("${spring.datasource.username:}")
    private String username;
    
    @Value("${spring.datasource.password:}")
    private String password;
    
    @Value("${app.config.file}")
    private String configFilePath;
    
    private final Environment env;
    private HikariDataSource dataSourceInstance;
    
    public AppConfig(Environment env) {
        this.env = env;
    }
    
    @PostConstruct
    public void init() {
        log.info("应用启动模式: {}", dataSourceEnabled ? "数据库模式" : "无数据库模式");
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            log.info("外部配置文件已加载: {}", configFilePath);
        } else {
            log.warn("外部配置文件不存在: {}", configFilePath);
        }
    }
    
    /**
     * 有条件地创建数据源
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "true")
    public DataSource dataSource() {
        try {
            log.info("尝试创建数据库连接...");
            dataSourceInstance = new HikariDataSource();
            dataSourceInstance.setDriverClassName(driverClassName);
            dataSourceInstance.setJdbcUrl(url);
            dataSourceInstance.setUsername(username);
            dataSourceInstance.setPassword(password);
            
            // 连接池配置
            dataSourceInstance.setConnectionTimeout(Long.parseLong(env.getProperty("spring.datasource.hikari.connection-timeout", "30000")));
            dataSourceInstance.setMaximumPoolSize(Integer.parseInt(env.getProperty("spring.datasource.hikari.maximum-pool-size", "10")));
            dataSourceInstance.setMinimumIdle(Integer.parseInt(env.getProperty("spring.datasource.hikari.minimum-idle", "5")));
            dataSourceInstance.setIdleTimeout(Long.parseLong(env.getProperty("spring.datasource.hikari.idle-timeout", "600000")));
            dataSourceInstance.setMaxLifetime(Long.parseLong(env.getProperty("spring.datasource.hikari.max-lifetime", "1800000")));
            
            // 验证连接是否有效
            dataSourceInstance.setConnectionTestQuery("SELECT 1");
            
            log.info("数据库连接创建成功: {}", url);
            return dataSourceInstance;
        } catch (Exception e) {
            log.error("创建数据库连接失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 检查数据源是否已启用
     */
    public boolean isDataSourceEnabled() {
        return dataSourceEnabled;
    }
    
    /**
     * 重新加载配置的方法，允许配置在不重启应用的情况下生效
     * 注意：这个方法应该在配置更新后调用
     */
    public void reloadConfig() {
        try {
            log.info("尝试重新加载配置...");
            File configFile = new File(configFilePath);
            
            if (!configFile.exists()) {
                log.warn("外部配置文件不存在，无法重新加载: {}", configFilePath);
                return;
            }
            
            // 读取最新的配置
            Properties properties = new Properties();
            properties.load(Files.newInputStream(Paths.get(configFilePath)));
            
            boolean newDataSourceEnabled = Boolean.parseBoolean(properties.getProperty("spring.datasource.enabled", "false"));
            String newDriverClassName = properties.getProperty("spring.datasource.driver-class-name", "");
            String newUrl = properties.getProperty("spring.datasource.url", "");
            String newUsername = properties.getProperty("spring.datasource.username", "");
            String newPassword = properties.getProperty("spring.datasource.password", "");
            
            // 更新实例变量
            this.dataSourceEnabled = newDataSourceEnabled;
            this.driverClassName = newDriverClassName;
            this.url = newUrl;
            this.username = newUsername;
            this.password = newPassword;
            
            // 如果数据源已存在，关闭它
            if (dataSourceInstance != null) {
                log.info("关闭现有数据库连接...");
                dataSourceInstance.close();
                dataSourceInstance = null;
            }
            
            // 如果启用了新的数据源，创建它
            if (newDataSourceEnabled) {
                log.info("根据新配置创建数据库连接...");
                dataSource();
            }
            
            log.info("配置重新加载完成");
        } catch (IOException e) {
            log.error("重新加载配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("重新加载配置失败", e);
        }
    }
} 