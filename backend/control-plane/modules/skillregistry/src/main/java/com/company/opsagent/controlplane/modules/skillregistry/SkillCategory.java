package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 分类枚举。
 *
 * <p>分类用于描述 Skill 主要服务的运维场景，便于后续做目录聚合、候选过滤和路由推荐。
 */
public enum SkillCategory {
  /**
   * 主机、容器、节点等基础设施诊断。
   */
  INFRASTRUCTURE_DIAGNOSTICS,

  /**
   * 应用进程、日志、线程、配置等应用级诊断。
   */
  APPLICATION_DIAGNOSTICS,

  /**
   * 平台层事件、告警、审计、指标等观测能力。
   */
  PLATFORM_OBSERVABILITY
}
