package com.news.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 数据库配置类
 * 用于条件化创建数据库相关的 Bean
 */
@Configuration
public class DatabaseConfig {

    /**
     * 创建一个空的数据源
     * 在数据源禁用时使用
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "false", matchIfMissing = true)
    public DataSource emptyDataSource() {
        return new EmptyDataSource();
    }

    /**
     * 创建 JdbcTemplate
     * 仅在数据源启用时创建
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "true")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 创建 NamedParameterJdbcTemplate
     * 仅在数据源启用时创建
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "true")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * 创建一个空的 JdbcTemplate 实现
     * 在数据源禁用时创建
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "false", matchIfMissing = true)
    public JdbcTemplate emptyJdbcTemplate(DataSource emptyDataSource) {
        return new EmptyJdbcTemplate(emptyDataSource);
    }

    /**
     * 创建一个空的 NamedParameterJdbcTemplate
     * 在数据源禁用时创建
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "false", matchIfMissing = true)
    public NamedParameterJdbcTemplate emptyNamedParameterJdbcTemplate(JdbcTemplate emptyJdbcTemplate) {
        return new NamedParameterJdbcTemplate(emptyJdbcTemplate);
    }
    
    /**
     * 空的 DataSource 实现
     * 所有方法都返回空值或抛出异常
     */
    private static class EmptyDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("数据源未配置");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("数据源未配置");
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
    
    /**
     * 空的 JdbcTemplate 实现
     * 所有方法都返回空值或空集合
     */
    private static class EmptyJdbcTemplate extends JdbcTemplate {
        public EmptyJdbcTemplate(DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public <T> T query(String sql, ResultSetExtractor<T> rse) {
            return null;
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
            return Collections.emptyList();
        }

        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType) {
            return null;
        }

        @Override
        public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) {
            return null;
        }

        @Override
        public Map<String, Object> queryForMap(String sql) {
            return Collections.emptyMap();
        }

        @Override
        public List<Map<String, Object>> queryForList(String sql) {
            return Collections.emptyList();
        }

        @Override
        public int update(String sql) {
            return 0;
        }

        @Override
        public int[] batchUpdate(String... sql) {
            return new int[0];
        }
    }
}