package com.news.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 数据库配置类
 */
@Slf4j
@Configuration
public class DatabaseConfig {
    
    private final AppConfig appConfig;
    
    @Autowired
    public DatabaseConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    /**
     * 创建数据源
     */
    @Bean
    public DataSource dataSource() {
        if (!appConfig.isDataSourceEnabled()) {
            log.warn("数据源未启用，返回空数据源");
            return new EmptyDataSource();
        }
        
        try {
            Properties properties = appConfig.getConfigProperties();
            
            String driverClassName = properties.getProperty("spring.datasource.driver-class-name");
            String url = properties.getProperty("spring.datasource.url");
            String username = properties.getProperty("spring.datasource.username");
            String password = properties.getProperty("spring.datasource.password");
            
            // 验证必要的配置参数
            if (driverClassName == null || driverClassName.trim().isEmpty() ||
                    url == null || url.trim().isEmpty() ||
                    username == null || username.trim().isEmpty()) {
                log.error("数据源配置不完整");
                return new EmptyDataSource();
            }
            
            // 创建数据源
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(driverClassName.trim());
            dataSource.setUrl(url.trim());
            dataSource.setUsername(username.trim());
            if (password != null) {
                dataSource.setPassword(password.trim());
            }
            
            log.info("数据源创建成功: url={}", url);
            return dataSource;
            
        } catch (Exception e) {
            log.error("创建数据源失败: {}", e.getMessage(), e);
            return new EmptyDataSource();
        }
    }
    
    /**
     * 创建 JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        if (dataSource instanceof EmptyDataSource) {
            log.warn("数据源未启用，返回空 JdbcTemplate");
            return new EmptyJdbcTemplate();
        }
        return new JdbcTemplate(dataSource);
    }
    
    /**
     * 空数据源实现
     */
    private static class EmptyDataSource implements DataSource {
        @Override
        public java.sql.Connection getConnection() {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public java.sql.Connection getConnection(String username, String password) {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public java.io.PrintWriter getLogWriter() {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public void setLogWriter(java.io.PrintWriter out) {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public void setLoginTimeout(int seconds) {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public int getLoginTimeout() {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public java.util.logging.Logger getParentLogger() {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) {
            throw new UnsupportedOperationException("数据源未启用");
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }
    
    /**
     * 空 JdbcTemplate 实现
     */
    private static class EmptyJdbcTemplate extends JdbcTemplate {
        @Override
        public <T> T execute(String sql, Object[] args, int[] argTypes, java.sql.PreparedStatementCallback<T> action) {
            throw new UnsupportedOperationException("数据源未启用");
        }
    }
}