package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * 发布校验流水中的单步结果。
 *
 * @param stepName 校验步骤名称
 * @param passed 是否通过
 * @param details 结果说明
 */
public record SkillPublicationValidationCheck(
    String stepName,
    boolean passed,
    String details) {
}
