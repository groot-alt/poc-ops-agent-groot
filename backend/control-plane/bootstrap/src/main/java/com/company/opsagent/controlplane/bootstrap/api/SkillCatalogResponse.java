package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.skillregistry.RegisteredSkill;
import java.util.List;

/**
 * Skill 目录列表响应。
 *
 * @param total 当前返回的 Skill 条目总数
 * @param skills 已注册 Skill 列表
 */
public record SkillCatalogResponse(
    int total,
    List<RegisteredSkill> skills) {

  /**
   * 对列表做防御性复制，避免序列化前被外部改写。
   */
  public SkillCatalogResponse {
    skills = List.copyOf(skills);
  }
}
