package com.news.service.controller;

import com.news.service.model.ApiResponse;
import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import com.news.service.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
@Api(tags = "搜索API", description = "提供新闻内容搜索功能")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 搜索新闻内容
     */
    @PostMapping
    @ApiOperation(value = "搜索新闻内容", notes = "通过关键词搜索新闻内容，支持分页和排序")
    public ResponseEntity<?> search(
            @ApiParam(value = "搜索请求")
            @RequestBody SearchRequest request
    ) {
        try {
            if (!searchService.isAvailable()) {
                return ResponseEntity.ok(ApiResponse.error("搜索服务不可用，请先配置数据库连接"));
            }
            
            if (request.getKeywords() == null || request.getKeywords().trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("搜索关键词不能为空"));
            }
            
            // 执行搜索
            List<SearchResult> results = searchService.search(request);
            
            // 获取总记录数
            long total = searchService.count(request.getKeywords());
            
            // 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("content", results);
            data.put("totalElements", total);
            data.put("totalPages", (total + request.getSize() - 1) / request.getSize());
            data.put("size", request.getSize());
            data.put("page", request.getPage());
            
            return ResponseEntity.ok(ApiResponse.success("搜索成功", data));
        } catch (Exception e) {
            log.error("搜索失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }
} 