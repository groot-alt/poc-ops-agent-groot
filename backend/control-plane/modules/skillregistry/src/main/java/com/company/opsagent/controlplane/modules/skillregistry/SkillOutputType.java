package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 输出类型枚举。
 *
 * <p>输出类型用于描述调用方应该怎样消费 Skill 结果，例如结构化 JSON、
 * 面向人阅读的 Markdown，或者适合表格展示的结果集。
 */
public enum SkillOutputType {
  /**
   * 结构化 JSON 对象或数组。
   */
  JSON,

  /**
   * 纯文本输出。
   */
  TEXT,

  /**
   * 表格化输出。
   */
  TABLE,

  /**
   * Markdown 格式输出。
   */
  MARKDOWN
}
