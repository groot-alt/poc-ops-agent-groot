package com.company.opsagent.controlplane.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 控制面连接独立执行 Worker 的配置。
 */
@ConfigurationProperties(prefix = "ops-agent.worker")
public class WorkerProperties {

  private String baseUrl = "http://127.0.0.1:8091";

  /**
   * 返回 Worker 基础地址。
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * 设置 Worker 基础地址。
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
