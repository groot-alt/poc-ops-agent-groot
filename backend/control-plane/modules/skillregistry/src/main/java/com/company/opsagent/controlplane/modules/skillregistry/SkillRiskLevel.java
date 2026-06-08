package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 风险等级枚举。
 *
 * <p>P1 阶段当前只允许登记只读诊断能力，因此现阶段实际落地清单应使用
 * {@link #READ_ONLY}。其余枚举先作为后续受控变更阶段的契约预留。
 */
public enum SkillRiskLevel {
  /**
   * 只读诊断，不允许对生产对象产生写入副作用。
   */
  READ_ONLY,

  /**
   * 低风险受控变更。
   */
  LOW,

  /**
   * 中风险受控变更。
   */
  MEDIUM,

  /**
   * 高风险操作，需要更严格审批和隔离。
   */
  HIGH
}
