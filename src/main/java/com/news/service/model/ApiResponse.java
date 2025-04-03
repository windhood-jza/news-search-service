package com.news.service.model;

import lombok.Data;
import java.io.Serializable;

/**
 * API 响应对象
 * @param <T> 数据类型
 */
@Data
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private T data;
    
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * 创建成功响应（仅包含数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "成功", data);
    }
    
    /**
     * 创建错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
} 