package com.news.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "true")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl("jdbc:oracle:thin:@localhost:1521:XE");
        dataSource.setUsername("system");
        dataSource.setPassword("password");
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "true")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "false")
    public DataSource emptyDataSource() {
        return new EmptyDataSource();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "false")
    public JdbcTemplate emptyJdbcTemplate() {
        return new EmptyJdbcTemplate();
    }

    private static class EmptyDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.io.PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            throw new UnsupportedOperationException();
        }
    }

    private static class EmptyJdbcTemplate extends JdbcTemplate {
        @Override
        public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) {
            return null;
        }

        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType) {
            return null;
        }

        @Override
        public <T> java.util.List<T> query(String sql, Object[] args, org.springframework.jdbc.core.RowMapper<T> rowMapper) {
            return new java.util.ArrayList<>();
        }
    }
}
