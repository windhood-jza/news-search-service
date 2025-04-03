package com.news.service.model;

import lombok.Data;
import java.util.List;

/**
 * 分页结果对象
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    /**
     * 当前页结果集
     */
    private List<T> content;
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页记录数
     */
    private int size;
    
    /**
     * 总记录数
     */
    private long totalElements;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 是否为第一页
     */
    private boolean first;
    
    /**
     * 是否为最后一页
     */
    private boolean last;
    
    /**
     * 是否为空结果
     */
    private boolean empty;
    
    /**
     * 当前页实际元素数量
     */
    private int numberOfElements;
    
    /**
     * 创建分页结果对象
     */
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        PageResult<T> result = new PageResult<>();
        
        result.setContent(content);
        result.setPage(page);
        result.setSize(size);
        result.setTotalElements(totalElements);
        
        // 计算总页数
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) size);
        result.setTotalPages(totalPages);
        
        // 设置其他分页属性
        result.setFirst(page == 0);
        result.setLast(page >= totalPages - 1);
        result.setEmpty(content.isEmpty());
        result.setNumberOfElements(content.size());
        
        return result;
    }
} 