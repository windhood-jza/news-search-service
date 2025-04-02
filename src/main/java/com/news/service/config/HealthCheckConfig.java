package com.news.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckConfig implements HealthIndicator {

    @Autowired
    private AppConfig appConfig;

    @Override
    public Health health() {
        if (!appConfig.isDataSourceEnabled()) {
            return Health.down()
                    .withDetail("message", "数据源未配置")
                    .withDetail("status", "DOWN")
                    .build();
        }

        return Health.up()
                .withDetail("message", "服务运行正常")
                .withDetail("status", "UP")
                .build();
    }
}
