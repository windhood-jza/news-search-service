package com.news.service.controller;

import com.news.service.model.ApiResponse;
import com.news.service.model.PageResult;
import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import com.news.service.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        logger.info("收到搜索请求：{}", request);

        if (request.getKeywords() == null || request.getKeywords().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "关键词不能为空"));
        }

        PageResult<SearchResult> results = searchService.search(
                request.getKeywords(),
                request.getPage(),
                request.getSize()
        );

        if (results == null) {
            return ResponseEntity.status(503).body(new ApiResponse(false, "服务暂时不可用"));
        }

        logger.info("搜索完成，共找到 {} 条结果", results.getTotalElements());
        return ResponseEntity.ok(new ApiResponse(true, "搜索成功", results));
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String keywords) {
        logger.info("收到统计请求，关键词：{}", keywords);

        if (keywords == null || keywords.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "关键词不能为空"));
        }

        int count = searchService.count(keywords);
        if (count < 0) {
            return ResponseEntity.status(503).body(new ApiResponse(false, "服务暂时不可用"));
        }

        logger.info("统计完成，共找到 {} 条结果", count);
        return ResponseEntity.ok(new ApiResponse(true, "统计成功", count));
    }
}
