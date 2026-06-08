package com.company.opsagent.controlplane.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 审计存储配置属性。
 *
 * @param storageMode 审计存储模式，当前主要使用文件方式
 * @param storagePath 审计文件落盘路径
 */
@ConfigurationProperties(prefix = "ops-agent.audit")
public record AuditProperties(
    String storageMode,
    String storagePath) {
}
