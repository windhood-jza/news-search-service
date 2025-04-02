package com.news.service.model;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装类
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    private List<T> content;      // 当前页的数据列表
    private int page;            // 当前页码
    private int size;            // 每页大小
    private long total;          // 总记录数
    private int totalPages;      // 总页数
    private boolean first;       // 是否为第一页
    private boolean last;        // 是否为最后一页
    private boolean hasNext;     // 是否有下一页
    private boolean hasPrevious; // 是否有上一页
    
    /**
     * 创建分页结果对象
     */
    public static <T> PageResult<T> of(List<T> content, int page, int size, long total) {
        PageResult<T> result = new PageResult<>();
        result.setContent(content);
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        
        // 计算总页数
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
        result.setTotalPages(totalPages);
        
        // 计算是否为第一页/最后一页
        result.setFirst(page == 0);
        result.setLast(page >= totalPages - 1);
        
        // 计算是否有上一页/下一页
        result.setHasNext(page < totalPages - 1);
        result.setHasPrevious(page > 0);
        
        return result;
    }
}