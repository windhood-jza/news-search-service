package com.news.service.model;

import lombok.Data;

/**
 * 搜索请求参数封装
 */
@Data
public class SearchRequest {
    private String keywords;        // 搜索关键词
    private int page = 0;          // 当前页码，默认为0
    private int size = 10;         // 每页大小，默认为10
    private String sortField;      // 排序字段
    private String sortDirection;  // 排序方向
    
    /**
     * 验证请求参数
     */
    public boolean validate() {
        // 关键词不能为空
        if (keywords == null || keywords.trim().isEmpty()) {
            return false;
        }
        
        // 页码和每页大小必须大于等于0
        if (page < 0 || size <= 0) {
            return false;
        }
        
        return true;
    }
}