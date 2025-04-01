package com.news.service.service;

import com.news.service.config.AppConfig;
import com.news.service.model.DatabaseConfig;
import com.news.service.model.DatabaseInfo;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置服务实现类
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {
    
    @Value("${app.config.file}")
    private String configFilePath;
    
    private final AppConfig appConfig;
    
    public ConfigServiceImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    @Override
    public String saveDatabaseConfig(DatabaseConfig config) {
        try {
            // 确保配置目录存在
            File configFile = new File(configFilePath);
            File configDir = configFile.getParentFile();
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            // 读取现有配置（如果存在）
            Properties properties = new Properties();
            if (configFile.exists()) {
                properties.load(Files.newInputStream(Paths.get(configFilePath)));
            }
            
            // 更新配置
            properties.setProperty("spring.datasource.enabled", String.valueOf(config.isDataSourceEnabled()));
            properties.setProperty("spring.datasource.driver-class-name", config.getDriverClassName());
            properties.setProperty("spring.datasource.url", config.getUrl());
            properties.setProperty("spring.datasource.username", config.getUsername());
            properties.setProperty("spring.datasource.password", config.getPassword());
            
            // 保存配置
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                properties.store(fos, "Database configuration updated");
            }
            
            log.info("数据库配置已保存到: {}", configFilePath);
            
            // 尝试立即重新加载配置（无需重启应用）
            try {
                log.info("尝试无需重启即时应用新配置...");
                appConfig.reloadConfig();
                log.info("新配置已应用");
            } catch (Exception e) {
                log.warn("配置已保存，但无法立即应用: {}", e.getMessage());
                log.warn("请重启应用以应用新配置");
            }
            
            return configFilePath;
        } catch (IOException e) {
            log.error("保存数据库配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存数据库配置失败", e);
        }
    }
    
    @Override
    public boolean testDatabaseConnection(DatabaseConfig config) {
        if (!config.isDataSourceEnabled()) {
            log.warn("数据库连接已禁用，无法测试连接");
            return false;
        }
        
        HikariDataSource dataSource = null;
        try {
            // 创建临时数据源
            dataSource = new HikariDataSource();
            dataSource.setDriverClassName(config.getDriverClassName());
            dataSource.setJdbcUrl(config.getUrl());
            dataSource.setUsername(config.getUsername());
            dataSource.setPassword(config.getPassword());
            dataSource.setConnectionTimeout(5000); // 5秒连接超时
            
            // 测试连接
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    log.info("数据库连接测试成功: {}", config.getUrl());
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("数据库连接测试失败: {}", e.getMessage(), e);
            return false;
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }
    
    @Override
    public DatabaseConfig getCurrentDatabaseConfig() {
        try {
            // 检查配置文件是否存在
            File configFile = new File(configFilePath);
            if (!configFile.exists()) {
                log.warn("配置文件不存在: {}", configFilePath);
                return new DatabaseConfig();
            }
            
            // 读取配置
            Properties properties = new Properties();
            properties.load(Files.newInputStream(Paths.get(configFilePath)));
            
            boolean enabled = Boolean.parseBoolean(properties.getProperty("spring.datasource.enabled", "false"));
            String driverClassName = properties.getProperty("spring.datasource.driver-class-name", "");
            String url = properties.getProperty("spring.datasource.url", "");
            String username = properties.getProperty("spring.datasource.username", "");
            String password = properties.getProperty("spring.datasource.password", "");
            
            // 创建并返回配置对象
            return new DatabaseConfig(enabled, driverClassName, url, username, password);
        } catch (IOException e) {
            log.error("读取数据库配置失败: {}", e.getMessage(), e);
            return new DatabaseConfig();
        }
    }
    
    @Override
    public String getDatabaseStatus() {
        return appConfig.isDataSourceEnabled() ? "ENABLED" : "DISABLED";
    }
    
    @Override
    public DatabaseInfo getDatabaseInfo() {
        if (!appConfig.isDataSourceEnabled()) {
            return null;
        }
        
        HikariDataSource dataSource = null;
        try {
            // 获取当前配置
            DatabaseConfig config = getCurrentDatabaseConfig();
            
            // 创建临时数据源
            dataSource = new HikariDataSource();
            dataSource.setDriverClassName(config.getDriverClassName());
            dataSource.setJdbcUrl(config.getUrl());
            dataSource.setUsername(config.getUsername());
            dataSource.setPassword(config.getPassword());
            dataSource.setConnectionTimeout(5000); // 5秒连接超时
            
            // 提取数据库信息
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                
                DatabaseInfo info = new DatabaseInfo();
                info.setDatabaseType(metaData.getDatabaseProductName());
                info.setDatabaseVersion(metaData.getDatabaseProductVersion());
                info.setUsername(config.getUsername());
                
                // 解析URL获取服务器地址和数据库名
                extractDatabaseInfoFromUrl(config.getUrl(), info);
                
                return info;
            }
        } catch (Exception e) {
            log.error("获取数据库信息失败: {}", e.getMessage(), e);
            return null;
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }
    
    /**
     * 从数据库URL中提取信息
     */
    private void extractDatabaseInfoFromUrl(String url, DatabaseInfo info) {
        try {
            // MySQL: jdbc:mysql://hostname:port/dbname
            Pattern mysqlPattern = Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)");
            Matcher mysqlMatcher = mysqlPattern.matcher(url);
            if (mysqlMatcher.find()) {
                info.setServerHost(mysqlMatcher.group(1));
                info.setDatabaseName(mysqlMatcher.group(3));
                return;
            }
            
            // PostgreSQL: jdbc:postgresql://hostname:port/dbname
            Pattern postgresPattern = Pattern.compile("jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+)");
            Matcher postgresMatcher = postgresPattern.matcher(url);
            if (postgresMatcher.find()) {
                info.setServerHost(postgresMatcher.group(1));
                info.setDatabaseName(postgresMatcher.group(3));
                return;
            }
            
            // Oracle: jdbc:oracle:thin:@hostname:port:sid
            Pattern oraclePattern = Pattern.compile("jdbc:oracle:thin:@([^:/]+)(?::(\\d+))?:([^?]+)");
            Matcher oracleMatcher = oraclePattern.matcher(url);
            if (oracleMatcher.find()) {
                info.setServerHost(oracleMatcher.group(1));
                info.setDatabaseName(oracleMatcher.group(3));
                return;
            }
            
            // SQL Server: jdbc:sqlserver://hostname:port;databaseName=dbname
            Pattern sqlServerPattern = Pattern.compile("jdbc:sqlserver://([^:/]+)(?::(\\d+))?;.*databaseName=([^;]+)");
            Matcher sqlServerMatcher = sqlServerPattern.matcher(url);
            if (sqlServerMatcher.find()) {
                info.setServerHost(sqlServerMatcher.group(1));
                info.setDatabaseName(sqlServerMatcher.group(3));
            }
        } catch (Exception e) {
            log.warn("解析数据库URL失败: {}", e.getMessage());
        }
    }
} 