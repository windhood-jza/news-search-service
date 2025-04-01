package com.news.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 数据库信息模型类
 * 用于存储和展示当前数据库连接的信息
 */
@ApiModel(description = "数据库连接信息")
public class DatabaseInfo {

    @ApiModelProperty(value = "数据库类型", example = "MySQL")
    private String databaseType;

    @ApiModelProperty(value = "数据库版本", example = "8.0.26")
    private String databaseVersion;

    @ApiModelProperty(value = "服务器主机地址", example = "localhost")
    private String serverHost;

    @ApiModelProperty(value = "数据库名称", example = "news")
    private String databaseName;

    @ApiModelProperty(value = "连接用户名", example = "root")
    private String username;

    public DatabaseInfo() {
    }

    public DatabaseInfo(String databaseType, String databaseVersion, String serverHost, String databaseName, String username) {
        this.databaseType = databaseType;
        this.databaseVersion = databaseVersion;
        this.serverHost = serverHost;
        this.databaseName = databaseName;
        this.username = username;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "DatabaseInfo{" +
                "databaseType='" + databaseType + '\'' +
                ", databaseVersion='" + databaseVersion + '\'' +
                ", serverHost='" + serverHost + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
} 