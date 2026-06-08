package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 显式发布校验服务。
 *
 * <p>该服务不负责真正修改仓库内容，而是把一次发布检查抽象成显式动作，
 * 统一输出“校验步骤 + 是否通过 + 可注册记录”，供控制面、发布流水和人工审核复用。
 */
public interface SkillPublicationWorkflowService {

  /**
   * 校验指定相对路径的 Manifest 是否满足发布要求。
   *
   * @param relativeManifestPath 相对于 Skill 根目录的 Manifest 路径
   * @return 发布校验结果
   */
  SkillPublicationValidationResult validatePublication(String relativeManifestPath);
}
