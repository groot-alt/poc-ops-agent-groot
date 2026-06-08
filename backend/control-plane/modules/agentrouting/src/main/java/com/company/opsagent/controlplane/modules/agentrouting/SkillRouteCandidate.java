package com.company.opsagent.controlplane.modules.agentrouting;

import com.company.opsagent.controlplane.modules.skillregistry.RegisteredSkill;
import com.company.opsagent.controlplane.modules.skillregistry.SkillReleaseSnapshot;
import java.util.List;

/**
 * 路由候选结果。
 *
 * @param skill 命中的注册 Skill
 * @param releaseSnapshot 当前候选的发布态快照
 * @param score 当前候选的排序分值
 * @param matchedRules 命中原因列表
 */
public record SkillRouteCandidate(
    RegisteredSkill skill,
    SkillReleaseSnapshot releaseSnapshot,
    int score,
    List<String> matchedRules) {

  /**
   * 防御性复制命中原因列表。
   */
  public SkillRouteCandidate {
    matchedRules = List.copyOf(matchedRules);
  }
}
