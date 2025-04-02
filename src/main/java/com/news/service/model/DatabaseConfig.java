package com.news.service.model;

import lombok.Data;

/**
 * 数据库配置模型
 */
@Data
public class DatabaseConfig {
    private String url;              // 数据库URL
    private String username;         // 用户名
    private String password;         // 密码
    private String driverClassName;  // 驱动类名
    private boolean enabled;         // 是否启用
}