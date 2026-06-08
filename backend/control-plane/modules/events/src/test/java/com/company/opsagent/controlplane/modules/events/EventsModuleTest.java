package com.company.opsagent.controlplane.modules.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 事件模块元信息测试。
 */
class EventsModuleTest {

  @Test
  void exposesModuleId() {
    // 验证事件模块编号符合设计约定。
    assertEquals("M09", EventsModule.moduleId());
  }
}
