package com.news.service.model;

import lombok.Data;

/**
 * 搜索请求模型类
 */
@Data
public class SearchRequest {
    /**
     * 搜索关键词，多个关键词用逗号分隔
     */
    private String keywords;
    
    /**
     * 当前页码（从0开始）
     */
    private int page = 0;
    
    /**
     * 每页结果数
     */
    private int size = 10;
    
    /**
     * 排序字段
     */
    private String sortField = "CREATED";
    
    /**
     * 排序方向（asc或desc）
     */
    private String sortDirection = "desc";
} 