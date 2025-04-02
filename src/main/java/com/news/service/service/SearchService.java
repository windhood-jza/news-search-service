package com.news.service.service;

import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import com.news.service.model.PageResult;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 检查服务是否可用
     */
    boolean isAvailable();
    
    /**
     * 执行搜索
     *
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    PageResult<SearchResult> search(SearchRequest request);
    
    /**
     * 获取搜索结果总数
     *
     * @param keywords 关键词
     * @return 结果总数
     */
    long count(String keywords);
}