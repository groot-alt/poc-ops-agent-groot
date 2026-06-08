package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 执行器类型枚举。
 *
 * <p>执行器类型用于声明 Skill 将由哪一类运行时承载，当前阶段主要用于登记，
 * 真正执行联动会在后续 M04/M08 中继续展开。
 */
public enum SkillExecutorType {
  /**
   * 通过受限 Shell 或系统命令完成诊断。
   */
  SHELL,

  /**
   * 通过 HTTP / API 调用远端系统。
   */
  HTTP,

  /**
   * 通过工作流或编排引擎拼接多个步骤。
   */
  WORKFLOW
}
