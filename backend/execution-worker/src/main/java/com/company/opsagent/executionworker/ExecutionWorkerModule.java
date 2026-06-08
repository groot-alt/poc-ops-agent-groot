package com.company.opsagent.executionworker;

import java.util.List;

/**
 * 执行 Worker 模块元信息定义。
 *
 * <p>当前类集中声明 Worker 的模块编号和受控边界，便于后续执行面治理与能力扩展。
 */
public final class ExecutionWorkerModule {

  private ExecutionWorkerModule() {
  }

  /**
   * 返回执行 Worker 模块编号。
   */
  public static String moduleId() {
    return "M07";
  }

  /**
   * 返回当前 Worker 强制执行的安全边界列表。
   */
  public static List<String> enforcedBoundaries() {
    return List.of("workspace", "network", "credential", "process");
  }
}
