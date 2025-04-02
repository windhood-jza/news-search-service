package com.news.service.controller;

import com.news.service.model.ApiResponse;
import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import com.news.service.model.PageResult;
import com.news.service.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@Slf4j
@Controller
public class SearchController {
    
    private final SearchService searchService;
    
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * 搜索页面
     */
    @GetMapping("/search")
    public String searchPage(Model model) {
        model.addAttribute("searchRequest", new SearchRequest());
        return "search";
    }
    
    /**
     * 执行搜索
     */
    @PostMapping("/search")
    public String search(@ModelAttribute SearchRequest request, Model model) {
        log.info("接收到搜索请求: {}", request);
        
        try {
            if (!request.validate()) {
                model.addAttribute("error", "请输入有效的搜索条件");
                return "search";
            }
            
            if (!searchService.isAvailable()) {
                model.addAttribute("error", "搜索服务不可用，请检查配置");
                return "search";
            }
            
            PageResult<SearchResult> results = searchService.search(request);
            model.addAttribute("results", results);
            model.addAttribute("searchRequest", request);
            
            log.info("搜索完成，找到 {} 条结果", results.getTotal());
            return "search";
            
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            model.addAttribute("error", "搜索过程中发生错误: " + e.getMessage());
            return "search";
        }
    }
    
    /**
     * 搜索API接口
     */
    @PostMapping("/api/search")
    @ResponseBody
    public ResponseEntity<ApiResponse<PageResult<SearchResult>>> searchApi(@RequestBody SearchRequest request) {
        log.info("接收到API搜索请求: {}", request);
        
        try {
            if (!request.validate()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("请输入有效的搜索条件"));
            }
            
            if (!searchService.isAvailable()) {
                return ResponseEntity.serviceUnavailable()
                        .body(ApiResponse.error("搜索服务不可用，请检查配置"));
            }
            
            PageResult<SearchResult> results = searchService.search(request);
            log.info("搜索完成，找到 {} 条结果", results.getTotal());
            
            return ResponseEntity.ok(ApiResponse.success(results));
            
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("搜索过程中发生错误: " + e.getMessage()));
        }
    }
    
    /**
     * 获取搜索结果总数
     */
    @GetMapping("/api/search/count")
    @ResponseBody
    public ResponseEntity<ApiResponse<Long>> count(@RequestParam String keywords) {
        log.info("接收到计数请求: keywords={}", keywords);
        
        try {
            if (keywords == null || keywords.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("请输入有效的关键词"));
            }
            
            if (!searchService.isAvailable()) {
                return ResponseEntity.serviceUnavailable()
                        .body(ApiResponse.error("搜索服务不可用，请检查配置"));
            }
            
            long count = searchService.count(keywords);
            log.info("计数完成，总共 {} 条结果", count);
            
            return ResponseEntity.ok(ApiResponse.success(count));
            
        } catch (Exception e) {
            log.error("计数失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("计数过程中发生错误: " + e.getMessage()));
        }
    }
}