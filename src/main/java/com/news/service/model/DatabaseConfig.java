package com.news.service.model;

import lombok.Data;
import java.io.Serializable;

/**
 * 数据库配置模型
 */
@Data
public class DatabaseConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private boolean dataSourceEnabled;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
} 