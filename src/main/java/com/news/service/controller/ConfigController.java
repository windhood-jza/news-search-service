package com.news.service.controller;

import com.news.service.config.AppConfig;
import com.news.service.model.ApiResponse;
import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置管理控制器
 */
@Slf4j
@Controller
public class ConfigController {
    
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
    public ConfigController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    /**
     * 配置页面
     */
    @GetMapping("/config")
    public String configPage(Model model) {
        DatabaseInfo info = new DatabaseInfo();
        info.setDriverClassName(driverClassName);
        info.setUrl(url);
        info.setUsername(username);
        info.setEnabled(appConfig.isDataSourceEnabled());
        
        model.addAttribute("databaseInfo", info);
        return "config";
    }
    
    /**
     * 更新数据库配置
     */
    @PostMapping("/api/config/database")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> updateDatabaseConfig(@RequestBody DatabaseConfig config) {
        log.info("接收到数据库配置更新请求");
        
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
            
            // 重新加载配置
            appConfig.reloadConfig();
            
            log.info("数据库配置更新成功");
            return ResponseEntity.ok(ApiResponse.success(null));
            
        } catch (Exception e) {
            log.error("更新数据库配置失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("更新配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前数据库配置信息
     */
    @GetMapping("/api/config/database")
    @ResponseBody
    public ResponseEntity<ApiResponse<DatabaseInfo>> getDatabaseConfig() {
        try {
            DatabaseInfo info = new DatabaseInfo();
            info.setDriverClassName(driverClassName);
            info.setUrl(url);
            info.setUsername(username);
            info.setEnabled(appConfig.isDataSourceEnabled());
            
            return ResponseEntity.ok(ApiResponse.success(info));
            
        } catch (Exception e) {
            log.error("获取数据库配置失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取配置失败: " + e.getMessage()));
        }
    }
}