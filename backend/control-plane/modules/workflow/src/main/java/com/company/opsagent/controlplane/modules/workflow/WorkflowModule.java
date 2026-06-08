package com.company.opsagent.controlplane.modules.workflow;

/**
 * 工作流模块元信息定义。
 */
public final class WorkflowModule {

  private WorkflowModule() {
  }

  /**
   * 返回工作流模块编号。
   */
  public static String moduleId() {
    return "M05";
  }
}
