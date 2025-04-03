package com.news.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * 搜索结果模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonIgnore
    private String id;
    private String name;
    private Date created;
    private String content;
    private double score;
    private String highlightedContent;
    
    /**
     * 创建SearchResult构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * SearchResult构建器类
     */
    public static class Builder {
        private String id;
        private String name;
        private Date created;
        private String content;
        private double score;
        private String highlightedContent;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder created(Date created) {
            this.created = created;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder score(double score) {
            this.score = score;
            return this;
        }
        
        public Builder highlightedContent(String highlightedContent) {
            this.highlightedContent = highlightedContent;
            return this;
        }
        
        public SearchResult build() {
            return new SearchResult(id, name, created, content, score, highlightedContent);
        }
    }
} 