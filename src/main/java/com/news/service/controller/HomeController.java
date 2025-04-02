package com.news.service.controller;

import com.news.service.config.AppConfig;
import com.news.service.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Slf4j
@Controller
public class HomeController {
    
    private final SearchService searchService;
    private final AppConfig appConfig;
    
    @Autowired
    public HomeController(SearchService searchService, AppConfig appConfig) {
        this.searchService = searchService;
        this.appConfig = appConfig;
    }
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String home(Model model) {
        // 检查服务状态
        boolean serviceAvailable = searchService.isAvailable();
        model.addAttribute("serviceAvailable", serviceAvailable);
        
        if (!serviceAvailable) {
            // 如果服务不可用，检查是否配置了数据库
            if (!appConfig.isDataSourceEnabled()) {
                model.addAttribute("setupRequired", true);
                model.addAttribute("message", "请先配置数据库连接");
            } else {
                model.addAttribute("setupRequired", false);
                model.addAttribute("message", "服务暂时不可用，请检查配置或稍后再试");
            }
        }
        
        return "home";
    }
    
    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    /**
     * 帮助页面
     */
    @GetMapping("/help")
    public String help() {
        return "help";
    }
    
    /**
     * API文档页面
     */
    @GetMapping("/api-docs")
    public String apiDocs() {
        return "api-docs";
    }
    
    /**
     * 状态页面
     */
    @GetMapping("/status")
    public String status(Model model) {
        model.addAttribute("serviceAvailable", searchService.isAvailable());
        model.addAttribute("databaseEnabled", appConfig.isDataSourceEnabled());
        return "status";
    }
}