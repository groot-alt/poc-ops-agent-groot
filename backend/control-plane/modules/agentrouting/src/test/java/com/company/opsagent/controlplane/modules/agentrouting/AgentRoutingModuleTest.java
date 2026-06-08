package com.company.opsagent.controlplane.modules.agentrouting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Agent 路由模块常量测试。
 */
class AgentRoutingModuleTest {

  @Test
  void exposesModuleId() {
    assertEquals("M04", AgentRoutingModule.moduleId());
  }
}
