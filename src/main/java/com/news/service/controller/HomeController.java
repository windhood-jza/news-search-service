package com.news.service.controller;

import com.news.service.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Slf4j
@Controller
public class HomeController {
    
    private final ConfigService configService;
    
    public HomeController(ConfigService configService) {
        this.configService = configService;
    }
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String home(Model model) {
        addDatabaseStatus(model);
        return "index";
    }
    
    /**
     * 搜索页面
     */
    @GetMapping("/search-page")
    public String searchPage(Model model) {
        addDatabaseStatus(model);
        return "search";
    }
    
    /**
     * 配置页面
     */
    @GetMapping("/config-page")
    public String configPage(Model model) {
        addDatabaseStatus(model);
        
        // 加载当前数据库配置
        model.addAttribute("dbConfig", configService.getCurrentDatabaseConfig());
        
        // 如果数据库已启用，加载数据库详细信息
        if ("ENABLED".equals(configService.getDatabaseStatus())) {
            model.addAttribute("databaseInfo", configService.getDatabaseInfo());
        }
        
        return "config";
    }
    
    /**
     * 添加数据库状态到模型
     */
    private void addDatabaseStatus(Model model) {
        String status = configService.getDatabaseStatus();
        boolean isDatabaseEnabled = "ENABLED".equals(status);
        
        model.addAttribute("databaseStatus", status);
        model.addAttribute("isDatabaseEnabled", isDatabaseEnabled);
        
        // 如果数据库已启用，添加数据库信息到所有页面，以便在导航栏显示数据库状态
        if (isDatabaseEnabled) {
            model.addAttribute("databaseInfo", configService.getDatabaseInfo());
        }
    }
} 