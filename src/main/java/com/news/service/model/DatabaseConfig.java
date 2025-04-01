package com.news.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 数据库配置模型类
 * 用于接收和返回数据库配置信息
 */
@ApiModel(description = "数据库配置信息")
public class DatabaseConfig {

    @ApiModelProperty(value = "是否启用数据库", required = true, example = "true")
    private boolean dataSourceEnabled;

    @ApiModelProperty(value = "数据库驱动类名", example = "com.mysql.cj.jdbc.Driver")
    private String driverClassName;

    @ApiModelProperty(value = "数据库连接URL", example = "jdbc:mysql://localhost:3306/news")
    private String url;

    @ApiModelProperty(value = "数据库用户名", example = "root")
    private String username;

    @ApiModelProperty(value = "数据库密码", example = "password")
    private String password;

    public DatabaseConfig() {
    }

    public DatabaseConfig(boolean dataSourceEnabled, String driverClassName, String url, String username, String password) {
        this.dataSourceEnabled = dataSourceEnabled;
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public boolean isDataSourceEnabled() {
        return dataSourceEnabled;
    }

    public void setDataSourceEnabled(boolean dataSourceEnabled) {
        this.dataSourceEnabled = dataSourceEnabled;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "dataSourceEnabled=" + dataSourceEnabled +
                ", driverClassName='" + driverClassName + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='********'" +
                '}';
    }
} 