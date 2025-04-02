package com.news.service.model;

import lombok.Data;

/**
 * 数据库配置信息
 */
@Data
public class DatabaseInfo {
    private String url;              // 数据库URL
    private String username;         // 用户名
    private String driverClassName;  // 驱动类名
    private boolean enabled;         // 是否启用
}