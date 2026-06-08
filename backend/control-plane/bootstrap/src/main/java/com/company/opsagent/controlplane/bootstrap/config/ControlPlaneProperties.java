package com.company.opsagent.controlplane.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 控制面服务基础属性。
 *
 * @param serviceName 对外暴露的服务名称，用于健康检查与模块清单返回
 * @param apiVersion 当前 API 版本号，用于 OpenAPI 文档和接口响应
 * @param apiDescription 当前控制面 API 的说明文本
 */
@ConfigurationProperties(prefix = "ops-agent.control-plane")
public record ControlPlaneProperties(
    String serviceName,
    String apiVersion,
    String apiDescription) {
}
