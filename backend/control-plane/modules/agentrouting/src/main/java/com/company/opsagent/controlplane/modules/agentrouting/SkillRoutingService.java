package com.company.opsagent.controlplane.modules.agentrouting;

import java.util.List;

/**
 * Skill 路由服务。
 */
public interface SkillRoutingService {

  /**
   * 按条件筛选并排序候选 Skill。
   *
   * @param criteria 路由筛选条件
   * @return 按优先级排序的候选列表
   */
  List<SkillRouteCandidate> findCandidates(SkillRoutingCriteria criteria);
}
