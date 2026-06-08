package com.company.opsagent.controlplane.modules.skillregistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件系统的 Skill 发布校验服务。
 *
 * <p>当前阶段将发布动作建模为一次显式校验流水：
 * 定位 Manifest -> 解析契约 -> 校验只读约束 -> 校验摘要与签名 -> 生成可注册记录。
 */
public class FileSystemSkillPublicationWorkflowService implements SkillPublicationWorkflowService {

  private final FileSystemSkillManifestLoader manifestLoader;

  public FileSystemSkillPublicationWorkflowService(FileSystemSkillManifestLoader manifestLoader) {
    this.manifestLoader = manifestLoader;
  }

  /**
   * 针对单个 Manifest 执行发布校验，并回传步骤化结果。
   */
  @Override
  public SkillPublicationValidationResult validatePublication(String relativeManifestPath) {
    List<SkillPublicationValidationCheck> checks = new ArrayList<>();
    try {
      Path rootPath = manifestLoader.resolvedRootPath();
      Path manifestPath = rootPath.resolve(relativeManifestPath).normalize();
      checks.add(new SkillPublicationValidationCheck(
          "locate-manifest",
          true,
          "已定位 Manifest: " + manifestPath));

      RegisteredSkill registeredSkill = manifestLoader.loadSingle(manifestPath);
      checks.add(new SkillPublicationValidationCheck(
          "validate-contract",
          true,
          "Skill 契约字段完整且满足 P1 只读约束"));
      checks.add(new SkillPublicationValidationCheck(
          "validate-publication",
          true,
          "摘要与 HMAC 签名校验通过，发布状态为 " + registeredSkill.publicationStatus()));
      checks.add(new SkillPublicationValidationCheck(
          "registerable",
          true,
          "该 Skill 可进入注册中心并参与后续路由筛选"));

      return new SkillPublicationValidationResult(
          registeredSkill.manifestPath(),
          true,
          checks,
          registeredSkill);
    } catch (RuntimeException exception) {
      checks.add(new SkillPublicationValidationCheck(
          "validate-publication",
          false,
          exception.getMessage()));
      return new SkillPublicationValidationResult(
          relativeManifestPath,
          false,
          checks,
          null);
    }
  }
}
