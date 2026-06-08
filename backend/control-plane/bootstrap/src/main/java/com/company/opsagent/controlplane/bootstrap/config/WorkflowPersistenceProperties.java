package com.company.opsagent.controlplane.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * M05 只读工作流持久化与启动恢复配置。
 */
@ConfigurationProperties(prefix = "ops-agent.workflow")
public class WorkflowPersistenceProperties {

  private boolean startupRecoveryEnabled = true;

  /**
   * 返回是否在控制面启动后扫描并恢复可重放工作流。
   */
  public boolean isStartupRecoveryEnabled() {
    return startupRecoveryEnabled;
  }

  /**
   * 设置是否启用启动恢复。
   */
  public void setStartupRecoveryEnabled(boolean startupRecoveryEnabled) {
    this.startupRecoveryEnabled = startupRecoveryEnabled;
  }
}
