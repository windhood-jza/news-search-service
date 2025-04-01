package com.news.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 * 分页结果模型类
 * 用于封装分页数据和分页信息
 * @param <T> 分页内容的类型
 */
@ApiModel(description = "分页结果数据")
public class PageResult<T> {

    @ApiModelProperty(value = "当前页内容")
    private List<T> content;

    @ApiModelProperty(value = "总元素数量", example = "100")
    private long totalElements;

    @ApiModelProperty(value = "总页数", example = "10")
    private int totalPages;

    @ApiModelProperty(value = "当前页码", example = "0")
    private int number;

    @ApiModelProperty(value = "每页大小", example = "10")
    private int size;

    @ApiModelProperty(value = "是否有下一页", example = "true")
    private boolean hasNext;

    @ApiModelProperty(value = "是否有上一页", example = "false")
    private boolean hasPrevious;

    @ApiModelProperty(value = "是否为第一页", example = "true")
    private boolean isFirst;

    @ApiModelProperty(value = "是否为最后一页", example = "false")
    private boolean isLast;

    public PageResult() {
    }

    public PageResult(List<T> content, long totalElements, int totalPages, int number, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
        this.hasNext = number < totalPages - 1;
        this.hasPrevious = number > 0;
        this.isFirst = number == 0;
        this.isLast = number == totalPages - 1;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
} 