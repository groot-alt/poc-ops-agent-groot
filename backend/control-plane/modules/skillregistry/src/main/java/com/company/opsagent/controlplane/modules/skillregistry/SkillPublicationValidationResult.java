package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.List;

/**
 * 显式发布校验动作结果。
 *
 * @param manifestPath 被校验的 Manifest 路径
 * @param passed 整体是否通过
 * @param checks 校验流水明细
 * @param registeredSkill 当校验通过时返回已解析的 Skill 记录
 */
public record SkillPublicationValidationResult(
    String manifestPath,
    boolean passed,
    List<SkillPublicationValidationCheck> checks,
    RegisteredSkill registeredSkill) {

  /**
   * 防御性复制校验结果列表。
   */
  public SkillPublicationValidationResult {
    checks = List.copyOf(checks);
  }
}
