package com.news.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * API通用响应类
 * 用于统一所有API接口的返回格式
 * @param <T> 响应数据类型
 */
@ApiModel(description = "API响应对象")
public class ApiResponse<T> {

    @ApiModelProperty(value = "响应是否成功", required = true)
    private boolean success;

    @ApiModelProperty(value = "响应消息")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "操作成功", null);
    }

    /**
     * 创建成功响应，包含数据
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }

    /**
     * 创建成功响应，包含消息和数据
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 创建失败响应
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(false, "操作失败", null);
    }

    /**
     * 创建失败响应，包含错误消息
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * 创建失败响应，包含错误消息和数据
     * @param message 错误消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    // Getter 和 Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
} 