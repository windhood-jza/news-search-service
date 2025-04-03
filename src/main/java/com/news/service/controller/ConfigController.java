package com.news.service.controller;

import com.news.service.config.AppConfig;
import com.news.service.model.ApiResponse;
import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;
import com.news.service.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 配置控制器
 */
@Slf4j
@RestController
@RequestMapping({"/config", "/news/config"})
public class ConfigController {
    
    private final ConfigService configService;
    private final AppConfig appConfig;
    private final ConfigurableApplicationContext applicationContext;
    
    public ConfigController(ConfigService configService, AppConfig appConfig, 
                           ConfigurableApplicationContext applicationContext) {
        this.configService = configService;
        this.appConfig = appConfig;
        this.applicationContext = applicationContext;
    }
    
    /**
     * 更新数据库配置
     */
    @PostMapping("/database")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateDatabaseConfig(
            @RequestBody DatabaseConfig config
    ) {
        try {
            String configFile = configService.saveDatabaseConfig(config);
            
            Map<String, String> data = new HashMap<>();
            data.put("configFile", configFile);
            data.put("message", "配置已保存。请使用重启功能使配置生效。");
            
            return ResponseEntity.ok(ApiResponse.success("数据库配置已保存", data));
        } catch (Exception e) {
            log.error("保存数据库配置失败", e);
            return ResponseEntity.status(400).body(ApiResponse.error("保存数据库配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重启应用
     */
    @PostMapping("/restart")
    public ResponseEntity<ApiResponse<Void>> restartApplication() {
        try {
            log.info("准备重启应用...");
            
            // 使用异步方式重启应用，确保响应能够返回给客户端
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000); // 等待1秒，确保响应能够返回
                    applicationContext.close();
                    System.exit(0); // 退出应用，依赖外部进程管理器（如 Tomcat）重启应用
                } catch (Exception e) {
                    log.error("重启应用失败", e);
                }
            });
            
            return ResponseEntity.ok(ApiResponse.success("应用正在重启，请稍后刷新页面", null));
        } catch (Exception e) {
            log.error("触发应用重启失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error("触发应用重启失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试数据库连接
     */
    @PostMapping("/database/test")
    public ResponseEntity<ApiResponse<Map<String, String>>> testDatabaseConnection(
            @RequestBody DatabaseConfig config
    ) {
        try {
            boolean testResult = configService.testDatabaseConnection(config);
            
            if (testResult) {
                Map<String, String> data = new HashMap<>();
                data.put("url", config.getUrl());
                
                return ResponseEntity.ok(ApiResponse.success("数据库连接测试成功", data));
            } else {
                return ResponseEntity.status(400).body(ApiResponse.error("数据库连接测试失败"));
            }
        } catch (Exception e) {
            log.error("测试数据库连接失败", e);
            return ResponseEntity.status(400).body(ApiResponse.error("测试数据库连接失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前数据库配置
     */
    @GetMapping("/database")
    public ResponseEntity<ApiResponse<DatabaseConfig>> getCurrentDatabaseConfig() {
        try {
            DatabaseConfig config = configService.getCurrentDatabaseConfig();
            return ResponseEntity.ok(ApiResponse.success("获取数据库配置成功", config));
        } catch (Exception e) {
            log.error("获取数据库配置失败", e);
            return ResponseEntity.status(400).body(ApiResponse.error("获取数据库配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取数据库详细信息
     */
    @GetMapping("/database/info")
    public ResponseEntity<ApiResponse<DatabaseInfo>> getDatabaseInfo() {
        try {
            DatabaseInfo info = configService.getDatabaseInfo();
            if (info != null) {
                return ResponseEntity.ok(ApiResponse.success("获取数据库信息成功", info));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("数据库未连接或获取信息失败"));
            }
        } catch (Exception e) {
            log.error("获取数据库信息失败", e);
            return ResponseEntity.status(400).body(ApiResponse.error("获取数据库信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重新加载配置
     */
    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<Void>> reloadConfiguration() {
        try {
            log.info("手动触发配置重新加载...");
            appConfig.reloadConfig();
            
            return ResponseEntity.ok(ApiResponse.success("配置已重新加载", null));
        } catch (Exception e) {
            log.error("重新加载配置失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error("重新加载配置失败: " + e.getMessage()));
        }
    }
} 