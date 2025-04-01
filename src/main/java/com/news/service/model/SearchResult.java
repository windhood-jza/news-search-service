package com.news.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

/**
 * 搜索结果模型类
 * 表示单条搜索结果信息
 */
@ApiModel(description = "搜索结果项")
public class SearchResult {

    @ApiModelProperty(value = "新闻ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "新闻标题", example = "最新科技动态")
    private String name;

    @ApiModelProperty(value = "新闻内容摘要", example = "最近科技行业有许多突破性进展...")
    private String content;

    @ApiModelProperty(value = "创建时间", example = "2023-01-01T12:00:00.000Z")
    private Date created;

    public SearchResult() {
    }

    public SearchResult(Long id, String name, String content, Date created) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", content='" + (content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
                ", created=" + created +
                '}';
    }
} 