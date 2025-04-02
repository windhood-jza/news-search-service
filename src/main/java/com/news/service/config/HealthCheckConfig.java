package com.news.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;

/**
 * 健康检查配置
 */
@Configuration
public class HealthCheckConfig implements HealthIndicator {
    
    private final AppConfig appConfig;
    
    @Autowired
    public HealthCheckConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    @Override
    public Health health() {
        if (!appConfig.isDataSourceEnabled()) {
            return Health.down()
                    .withDetail("message", "数据源未配置")
                    .build();
        }
        
        return Health.up()
                .withDetail("message", "服务运行正常")
                .build();
    }
}