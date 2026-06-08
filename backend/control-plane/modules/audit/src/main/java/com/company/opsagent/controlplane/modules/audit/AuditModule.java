package com.company.opsagent.controlplane.modules.audit;

/**
 * 审计模块元信息定义。
 */
public final class AuditModule {

  private AuditModule() {
  }

  /**
   * 返回审计模块编号。
   */
  public static String moduleId() {
    return "M02";
  }
}
