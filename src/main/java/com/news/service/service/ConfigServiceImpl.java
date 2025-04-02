package com.news.service.service;

import com.news.service.config.AppConfig;
import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * 配置服务实现类
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {
    
    private final AppConfig appConfig;
    
    @Value("${app.config.file}")
    private String configFilePath;
    
    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;
    
    @Value("${spring.datasource.url:}")
    private String url;
    
    @Value("${spring.datasource.username:}")
    private String username;
    
    @Autowired
    public ConfigServiceImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    @Override
    public DatabaseInfo getDatabaseInfo() {
        DatabaseInfo info = new DatabaseInfo();
        info.setDriverClassName(driverClassName);
        info.setUrl(url);
        info.setUsername(username);
        info.setEnabled(appConfig.isDataSourceEnabled());
        return info;
    }
    
    @Override
    public void updateDatabaseConfig(DatabaseConfig config) {
        log.info("开始更新数据库配置");
        
        try {
            // 验证配置文件路径
            File configFile = new File(configFilePath);
            if (!configFile.exists()) {
                File parentDir = configFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        throw new IOException("创建配置文件目录失败");
                    }
                }
                if (!configFile.createNewFile()) {
                    throw new IOException("创建配置文件失败");
                }
            }
            
            // 读取现有配置
            Properties properties = new Properties();
            if (configFile.length() > 0) {
                properties.load(Files.newInputStream(Paths.get(configFilePath)));
            }
            
            // 更新配置
            properties.setProperty("spring.datasource.enabled", String.valueOf(config.isEnabled()));
            if (config.isEnabled()) {
                properties.setProperty("spring.datasource.driver-class-name", config.getDriverClassName());
                properties.setProperty("spring.datasource.url", config.getUrl());
                properties.setProperty("spring.datasource.username", config.getUsername());
                if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                    properties.setProperty("spring.datasource.password", config.getPassword());
                }
            } else {
                properties.remove("spring.datasource.driver-class-name");
                properties.remove("spring.datasource.url");
                properties.remove("spring.datasource.username");
                properties.remove("spring.datasource.password");
            }
            
            // 保存配置
            try (FileOutputStream out = new FileOutputStream(configFile)) {
                properties.store(out, "数据库配置");
            }
            
            log.info("数据库配置更新成功");
            
        } catch (Exception e) {
            log.error("更新数据库配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新配置失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String testDatabaseConnection(DatabaseConfig config) {
        log.info("开始测试数据库连接");
        
        if (!config.isEnabled()) {
            return "数据库连接已禁用";
        }
        
        try {
            // 加载驱动
            Class.forName(config.getDriverClassName());
            
            // 测试连接
            try (Connection conn = DriverManager.getConnection(
                    config.getUrl(),
                    config.getUsername(),
                    config.getPassword()
            )) {
                log.info("数据库连接测试成功");
                return "数据库连接测试成功";
            }
            
        } catch (ClassNotFoundException e) {
            log.error("数据库驱动加载失败: {}", e.getMessage(), e);
            return "数据库驱动加载失败: " + e.getMessage();
            
        } catch (Exception e) {
            log.error("数据库连接测试失败: {}", e.getMessage(), e);
            return "数据库连接测试失败: " + e.getMessage();
        }
    }
    
    @Override
    public void reloadConfig() {
        log.info("开始重新加载配置");
        appConfig.reloadConfig();
        log.info("配置重新加载完成");
    }
}