package com.company.opsagent.controlplane.modules.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 策略模块元信息测试。
 */
class PolicyModuleTest {

  @Test
  void exposesModuleId() {
    // 验证策略模块编号未偏离设计约定。
    assertEquals("M02", PolicyModule.moduleId());
  }
}
