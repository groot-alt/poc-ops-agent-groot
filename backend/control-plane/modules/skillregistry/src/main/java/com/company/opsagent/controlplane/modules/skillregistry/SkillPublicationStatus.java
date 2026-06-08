package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 发布状态枚举。
 *
 * <p>M03 当前只把通过契约、摘要和签名校验的 Skill 放入注册中心，因此现阶段
 * 主要使用 {@link #VALIDATED}。其余状态作为后续发布流水扩展预留。
 */
public enum SkillPublicationStatus {
  /**
   * 已通过发布校验，可供路由和执行流程引用。
   */
  VALIDATED,

  /**
   * 尚未经过正式发布校验。
   */
  DRAFT,

  /**
   * 发布校验失败或已被拒绝。
   */
  REJECTED
}
