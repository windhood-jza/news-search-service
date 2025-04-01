package com.news.service.controller;

import com.news.service.config.AppConfig;
import com.news.service.model.ApiResponse;
import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;
import com.news.service.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/config")
@Api(tags = "配置API", description = "提供数据库配置功能")
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
    @ApiOperation(value = "更新数据库配置", notes = "设置数据库连接信息并保存到配置文件")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateDatabaseConfig(
            @ApiParam(value = "数据库配置")
            @RequestBody DatabaseConfig config
    ) {
        try {
            String configFile = configService.saveDatabaseConfig(config);
            
            Map<String, String> data = new HashMap<>();
            data.put("configFile", configFile);
            data.put("message", "配置已保存并尝试即时应用。如果配置未生效，请使用重载配置功能。");
            
            return ResponseEntity.ok(ApiResponse.success("数据库配置已保存", data));
        } catch (Exception e) {
            log.error("保存数据库配置失败", e);
            return ResponseEntity.status(400).body(ApiResponse.error("保存数据库配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试数据库连接
     */
    @PostMapping("/database/test")
    @ApiOperation(value = "测试数据库连接", notes = "测试数据库连接是否有效")
    public ResponseEntity<ApiResponse<Map<String, String>>> testDatabaseConnection(
            @ApiParam(value = "数据库配置")
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
    @ApiOperation(value = "获取当前数据库配置", notes = "获取当前使用的数据库配置信息")
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
    @ApiOperation(value = "获取数据库详细信息", notes = "获取当前连接的数据库详细信息")
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
    @ApiOperation(value = "重新加载配置", notes = "尝试在不重启应用的情况下重新加载配置")
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