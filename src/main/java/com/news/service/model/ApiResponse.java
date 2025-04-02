package com.news.service.model;

import lombok.Data;

/**
 * API响应封装类
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> {
    private boolean success;  // 是否成功
    private String message;   // 响应消息
    private T data;          // 响应数据
    
    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}