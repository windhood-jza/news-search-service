package com.news.service.model;

import lombok.Data;
import java.io.Serializable;

/**
 * 数据库信息模型
 */
@Data
public class DatabaseInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String databaseType;
    private String databaseVersion;
    private String serverHost;
    private String databaseName;
    private String username;
} 