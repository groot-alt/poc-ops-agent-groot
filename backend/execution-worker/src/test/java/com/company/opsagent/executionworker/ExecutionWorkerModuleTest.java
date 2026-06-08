package com.company.opsagent.executionworker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 执行 Worker 模块元信息测试。
 */
class ExecutionWorkerModuleTest {

  @Test
  void exposesWorkerIdentityAndBoundaries() {
    // 验证执行 Worker 的模块编号和受控边界声明均已正确暴露。
    assertEquals("M07", ExecutionWorkerModule.moduleId());
    assertTrue(ExecutionWorkerModule.enforcedBoundaries().contains("network"));
  }
}
