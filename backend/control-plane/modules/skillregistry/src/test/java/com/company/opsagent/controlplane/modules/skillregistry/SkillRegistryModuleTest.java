package com.company.opsagent.controlplane.modules.skillregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Skill 注册中心模块常量测试。
 */
class SkillRegistryModuleTest {

  @Test
  void exposesModuleId() {
    // 验证模块编号与设计蓝图中的 M03 保持一致。
    assertEquals("M03", SkillRegistryModule.moduleId());
  }
}
