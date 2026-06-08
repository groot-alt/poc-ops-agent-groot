package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.List;

/**
 * Skill 发布态管理服务。
 *
 * <p>该服务负责维护“已通过发布校验的 Skill 在当前环境下是否全量、灰度或已回滚”。
 */
public interface SkillReleaseManagementService {

  /**
   * 返回某个已注册 Skill 的当前发布态。
   */
  SkillReleaseSnapshot snapshot(RegisteredSkill registeredSkill);

  /**
   * 将指定 Skill 版本推进到灰度阶段。
   */
  SkillReleaseSnapshot promoteCanary(
      String skillId,
      String version,
      int rolloutPercentage,
      List<String> targetContextTags,
      String reason);

  /**
   * 将指定 Skill 版本推进到全量可用阶段。
   */
  SkillReleaseSnapshot promoteGeneral(
      String skillId,
      String version,
      String reason);

  /**
   * 将指定 Skill 版本回滚。
   */
  SkillReleaseSnapshot rollback(
      String skillId,
      String version,
      String reason);
}
