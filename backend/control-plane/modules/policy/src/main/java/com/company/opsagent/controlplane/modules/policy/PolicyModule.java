package com.company.opsagent.controlplane.modules.policy;

/**
 * 策略模块元信息定义。
 */
public final class PolicyModule {

  private PolicyModule() {
  }

  /**
   * 返回策略模块编号。
   */
  public static String moduleId() {
    return "M02";
  }
}
