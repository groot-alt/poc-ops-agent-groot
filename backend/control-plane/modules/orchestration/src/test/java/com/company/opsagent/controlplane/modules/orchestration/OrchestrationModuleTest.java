package com.company.opsagent.controlplane.modules.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 编排模块元信息测试。
 */
class OrchestrationModuleTest {

  @Test
  void exposesModuleId() {
    // 验证编排模块编号符合设计约定。
    assertEquals("M06", OrchestrationModule.moduleId());
  }
}
