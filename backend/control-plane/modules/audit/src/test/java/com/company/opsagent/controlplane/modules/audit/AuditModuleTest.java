package com.company.opsagent.controlplane.modules.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 审计模块元信息测试。
 */
class AuditModuleTest {

  @Test
  void exposesModuleId() {
    // 验证审计模块编号保持稳定。
    assertEquals("M02", AuditModule.moduleId());
  }
}
