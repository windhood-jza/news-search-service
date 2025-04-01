package com.news.service.service;

import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;

/**
 * 配置服务接口
 */
public interface ConfigService {
    
    /**
     * 保存数据库配置
     * 
     * @param config 数据库配置
     * @return 保存配置的文件路径
     */
    String saveDatabaseConfig(DatabaseConfig config);
    
    /**
     * 测试数据库连接
     * 
     * @param config 数据库配置
     * @return 连接测试结果，true为成功，false为失败
     */
    boolean testDatabaseConnection(DatabaseConfig config);
    
    /**
     * 获取当前数据库配置
     * 
     * @return 当前数据库配置
     */
    DatabaseConfig getCurrentDatabaseConfig();
    
    /**
     * 获取数据库状态
     * 
     * @return 数据库状态，"ENABLED"或"DISABLED"
     */
    String getDatabaseStatus();
    
    /**
     * 获取数据库详细信息
     * 包括数据库类型、版本、主机、数据库名等
     * 
     * @return 数据库详细信息，如果数据库未启用则返回null
     */
    DatabaseInfo getDatabaseInfo();
} 