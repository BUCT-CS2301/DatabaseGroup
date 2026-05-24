package com.platform.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.backup")
public class BackupProperties {

    /** 备份文件落盘目录；为空时默认使用 classpath 下 backups 目录 */
    private String storageDirectory = "";

    private long totalSpaceMb = 20480;

    private String mysqldumpPath = "mysqldump";

    private String mysqlPath = "mysql";

    private int retentionDays = 30;

    private int maxConcurrentBackups = 1;

    /** IN_PROGRESS 超过该分钟数视为僵死任务，自动标记 FAILED */
    private int inProgressTimeoutMinutes = 10;

    /** 非空时通过 docker exec 在容器内执行 mysqldump/mysql（开发环境推荐 dev-mysql） */
    private String dockerContainer = "";

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory == null ? "" : storageDirectory;
    }

    public long getTotalSpaceMb() {
        return totalSpaceMb;
    }

    public void setTotalSpaceMb(long totalSpaceMb) {
        this.totalSpaceMb = totalSpaceMb;
    }

    public String getMysqldumpPath() {
        return mysqldumpPath;
    }

    public void setMysqldumpPath(String mysqldumpPath) {
        this.mysqldumpPath = mysqldumpPath;
    }

    public String getMysqlPath() {
        return mysqlPath;
    }

    public void setMysqlPath(String mysqlPath) {
        this.mysqlPath = mysqlPath;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public int getMaxConcurrentBackups() {
        return maxConcurrentBackups;
    }

    public void setMaxConcurrentBackups(int maxConcurrentBackups) {
        this.maxConcurrentBackups = maxConcurrentBackups;
    }

    public int getInProgressTimeoutMinutes() {
        return inProgressTimeoutMinutes;
    }

    public void setInProgressTimeoutMinutes(int inProgressTimeoutMinutes) {
        this.inProgressTimeoutMinutes = inProgressTimeoutMinutes;
    }

    public String getDockerContainer() {
        return dockerContainer;
    }

    public void setDockerContainer(String dockerContainer) {
        this.dockerContainer = dockerContainer == null ? "" : dockerContainer;
    }
}
