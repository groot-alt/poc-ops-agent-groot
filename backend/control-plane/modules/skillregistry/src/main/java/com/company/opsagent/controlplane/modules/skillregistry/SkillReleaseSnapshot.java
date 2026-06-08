package com.company.opsagent.controlplane.modules.skillregistry;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Skill 当前发布态快照。
 *
 * @param skillId Skill 稳定标识
 * @param version Skill 版本号
 * @param stage 当前发布阶段
 * @param rolloutPercentage 当前灰度百分比
 * @param targetContextTags 灰度命中的上下文标签
 * @param reason 最近一次阶段变化原因
 * @param updatedAt 最近一次阶段变化时间
 */
public record SkillReleaseSnapshot(
    String skillId,
    String version,
    SkillReleaseStage stage,
    int rolloutPercentage,
    List<String> targetContextTags,
    String reason,
    OffsetDateTime updatedAt) {

  /**
   * 防御性复制标签列表。
   */
  public SkillReleaseSnapshot {
    targetContextTags = targetContextTags == null ? List.of() : List.copyOf(targetContextTags);
  }
}
