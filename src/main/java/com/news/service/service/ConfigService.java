package com.news.service.service;

import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;

/**
 * 配置服务接口
 */
public interface ConfigService {
    
    /**
     * 获取当前数据库配置信息
     *
     * @return 数据库配置信息
     */
    DatabaseInfo getDatabaseInfo();
    
    /**
     * 更新数据库配置
     *
     * @param config 新的数据库配置
     */
    void updateDatabaseConfig(DatabaseConfig config);
    
    /**
     * 测试数据库连接
     *
     * @param config 数据库配置
     * @return 测试结果消息
     */
    String testDatabaseConnection(DatabaseConfig config);
    
    /**
     * 重新加载配置
     */
    void reloadConfig();
}