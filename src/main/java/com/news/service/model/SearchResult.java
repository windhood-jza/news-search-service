package com.news.service.model;

import lombok.Data;
import java.util.Date;

/**
 * 搜索结果模型
 */
@Data
public class SearchResult {
    private String id;                  // 文章ID
    private String name;                // 文章标题
    private String content;             // 文章内容
    private Date created;               // 创建时间
    private double score;               // 相关度分数
    private String highlightedContent;   // 高亮显示的内容
}