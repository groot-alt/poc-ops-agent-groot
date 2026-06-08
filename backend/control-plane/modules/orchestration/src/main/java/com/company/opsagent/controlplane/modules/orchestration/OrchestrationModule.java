package com.company.opsagent.controlplane.modules.orchestration;

/**
 * 编排模块元信息定义。
 */
public final class OrchestrationModule {

  private OrchestrationModule() {
  }

  /**
   * 返回编排模块编号。
   */
  public static String moduleId() {
    return "M06";
  }
}
