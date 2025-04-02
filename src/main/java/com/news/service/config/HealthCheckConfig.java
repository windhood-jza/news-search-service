package com.news.service.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 自定义健康检查配置
 */
@Component
public class HealthCheckConfig implements HealthIndicator {
    
    private final AppConfig appConfig;
    
    public HealthCheckConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    @Override
    public Health health() {
        if (!appConfig.isDataSourceEnabled()) {
            return Health.up()
                    .withDetail("database", "Database is not configured")
                    .withDetail("message", "Application is running in no-database mode")
                    .build();
        }
        
        return Health.up()
                .withDetail("database", "Database is configured")
                .withDetail("message", "Application is running in database mode")
                .build();
    }
}