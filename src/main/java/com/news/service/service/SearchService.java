package com.news.service.service;

import com.news.service.model.SearchRequest;
import com.news.service.model.SearchResult;
import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 搜索新闻内容
     * 
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    List<SearchResult> search(SearchRequest request);
    
    /**
     * 获取搜索结果总数
     *
     * @param keywords 搜索关键词
     * @return 结果总数
     */
    long count(String keywords);
    
    /**
     * 检查搜索服务是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
} 