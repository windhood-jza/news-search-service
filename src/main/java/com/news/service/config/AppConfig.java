package com.news.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 应用程序配置类
 */
@Slf4j
@Component
public class AppConfig implements ApplicationListener<ApplicationStartedEvent> {
    
    @Value("${app.config.file}")
    private String configFilePath;
    
    private boolean dataSourceEnabled;
    
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("应用程序启动，开始加载配置");
        loadConfig();
    }
    
    /**
     * 加载配置
     */
    private void loadConfig() {
        log.info("开始加载配置文件: {}", configFilePath);
        
        try {
            // 检查配置文件是否存在
            File configFile = new File(configFilePath);
            if (!configFile.exists()) {
                // 如果配置文件不存在，创建目录和文件
                File parentDir = configFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        throw new IOException("创建配置文件目录失败");
                    }
                }
                if (!configFile.createNewFile()) {
                    throw new IOException("创建配置文件失败");
                }
                
                // 创建默认配置
                Properties defaultProperties = new Properties();
                defaultProperties.setProperty("spring.datasource.enabled", "false");
                defaultProperties.store(Files.newOutputStream(Paths.get(configFilePath)), "默认配置");
                
                log.info("创建默认配置文件");
                dataSourceEnabled = false;
                return;
            }
            
            // 读取配置文件
            Properties properties = new Properties();
            try (FileInputStream in = new FileInputStream(configFile)) {
                properties.load(in);
            }
            
            // 解析配置
            String enabled = properties.getProperty("spring.datasource.enabled", "false");
            dataSourceEnabled = Boolean.parseBoolean(enabled);
            
            log.info("加载配置成功: dataSourceEnabled={}", dataSourceEnabled);
            
        } catch (Exception e) {
            log.error("加载配置文件失败: {}", e.getMessage(), e);
            dataSourceEnabled = false;
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        log.info("开始重新加载配置");
        loadConfig();
    }
    
    /**
     * 检查数据源是否启用
     */
    public boolean isDataSourceEnabled() {
        return dataSourceEnabled;
    }
    
    /**
     * 获取配置文件路径
     */
    public String getConfigFilePath() {
        return configFilePath;
    }
    
    /**
     * 获取配置文件内容
     */
    public Properties getConfigProperties() {
        Properties properties = new Properties();
        try {
            File configFile = new File(configFilePath);
            if (configFile.exists() && configFile.length() > 0) {
                try (FileInputStream in = new FileInputStream(configFile)) {
                    properties.load(in);
                }
            }
        } catch (Exception e) {
            log.error("读取配置文件失败: {}", e.getMessage(), e);
        }
        return properties;
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfigProperties(Properties properties) {
        try {
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
            properties.store(Files.newOutputStream(Paths.get(configFilePath)), "应用程序配置");
            log.info("保存配置文件成功");
        } catch (Exception e) {
            log.error("保存配置文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存配置文件失败: " + e.getMessage(), e);
        }
    }
}