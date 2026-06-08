package com.company.opsagent.controlplane.modules.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 身份模块元信息测试。
 */
class IdentityModuleTest {

  @Test
  void exposesModuleId() {
    // 验证模块编号与设计蓝图保持一致。
    assertEquals("M01", IdentityModule.moduleId());
  }
}
