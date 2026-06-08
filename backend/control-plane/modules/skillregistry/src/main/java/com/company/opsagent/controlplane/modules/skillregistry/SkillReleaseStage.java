package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 发布阶段枚举。
 */
public enum SkillReleaseStage {
  /**
   * 正式全量可用。
   */
  GENERAL_AVAILABLE,

  /**
   * 灰度阶段，仅对命中条件的请求开放。
   */
  CANARY,

  /**
   * 已回滚，不再参与路由。
   */
  ROLLED_BACK
}
