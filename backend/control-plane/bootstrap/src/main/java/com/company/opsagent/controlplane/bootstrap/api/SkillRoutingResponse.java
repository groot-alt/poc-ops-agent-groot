package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.agentrouting.SkillRouteCandidate;
import java.util.List;

/**
 * Skill 路由候选响应。
 *
 * @param total 命中的候选数量
 * @param candidates 排序后的候选列表
 */
public record SkillRoutingResponse(
    int total,
    List<SkillRouteCandidate> candidates) {

  /**
   * 防御性复制候选列表。
   */
  public SkillRoutingResponse {
    candidates = List.copyOf(candidates);
  }
}
