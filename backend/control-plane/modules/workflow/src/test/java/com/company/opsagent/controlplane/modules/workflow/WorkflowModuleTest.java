package com.company.opsagent.controlplane.modules.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 工作流模块元信息测试。
 */
class WorkflowModuleTest {

  @Test
  void exposesModuleId() {
    // 验证工作流模块编号符合设计约定。
    assertEquals("M05", WorkflowModule.moduleId());
  }
}
