package com.company.opsagent.controlplane.modules.skillregistry;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 基于内存的 Skill 发布态管理服务。
 *
 * <p>P1 阶段先用内存方式表达灰度和回滚状态，后续可以替换成数据库或配置中心。
 */
public class InMemorySkillReleaseManagementService implements SkillReleaseManagementService {

  private final SkillRegistryService skillRegistryService;
  private final Map<String, SkillReleaseSnapshot> snapshotsByKey = new LinkedHashMap<>();

  public InMemorySkillReleaseManagementService(SkillRegistryService skillRegistryService) {
    this.skillRegistryService = skillRegistryService;
    for (RegisteredSkill skill : skillRegistryService.listSkills()) {
      snapshotsByKey.put(key(skill.descriptor().skillId(), skill.descriptor().version()), defaultSnapshot(skill));
    }
  }

  /**
   * 返回某个注册 Skill 的当前发布态；如果尚未显式变更，则返回默认全量态。
   */
  @Override
  public SkillReleaseSnapshot snapshot(RegisteredSkill registeredSkill) {
    return snapshotsByKey.computeIfAbsent(
        key(registeredSkill.descriptor().skillId(), registeredSkill.descriptor().version()),
        ignored -> defaultSnapshot(registeredSkill));
  }

  /**
   * 推进到灰度阶段。
   */
  @Override
  public SkillReleaseSnapshot promoteCanary(
      String skillId,
      String version,
      int rolloutPercentage,
      List<String> targetContextTags,
      String reason) {
    RegisteredSkill skill = resolve(skillId, version);
    SkillReleaseSnapshot snapshot = new SkillReleaseSnapshot(
        skillId,
        version,
        SkillReleaseStage.CANARY,
        rolloutPercentage,
        targetContextTags,
        normalizeReason(reason, "promoted to canary"),
        OffsetDateTime.now());
    snapshotsByKey.put(key(skillId, version), snapshot);
    return snapshot;
  }

  /**
   * 推进到全量阶段。
   */
  @Override
  public SkillReleaseSnapshot promoteGeneral(String skillId, String version, String reason) {
    RegisteredSkill skill = resolve(skillId, version);
    SkillReleaseSnapshot snapshot = new SkillReleaseSnapshot(
        skillId,
        version,
        SkillReleaseStage.GENERAL_AVAILABLE,
        100,
        List.of(),
        normalizeReason(reason, "promoted to general"),
        OffsetDateTime.now());
    snapshotsByKey.put(key(skillId, version), snapshot);
    return snapshot;
  }

  /**
   * 回滚指定版本。
   */
  @Override
  public SkillReleaseSnapshot rollback(String skillId, String version, String reason) {
    RegisteredSkill skill = resolve(skillId, version);
    SkillReleaseSnapshot snapshot = new SkillReleaseSnapshot(
        skillId,
        version,
        SkillReleaseStage.ROLLED_BACK,
        0,
        List.of(),
        normalizeReason(reason, "rolled back"),
        OffsetDateTime.now());
    snapshotsByKey.put(key(skillId, version), snapshot);
    return snapshot;
  }

  private RegisteredSkill resolve(String skillId, String version) {
    return skillRegistryService.findByVersion(skillId, version)
        .orElseThrow(() -> new IllegalArgumentException("skill version not found: " + skillId + ":" + version));
  }

  private SkillReleaseSnapshot defaultSnapshot(RegisteredSkill skill) {
    return new SkillReleaseSnapshot(
        skill.descriptor().skillId(),
        skill.descriptor().version(),
        SkillReleaseStage.GENERAL_AVAILABLE,
        100,
        List.of(),
        "default validated release",
        skill.publication().publishedAt());
  }

  private String key(String skillId, String version) {
    return normalize(skillId) + ":" + normalize(version);
  }

  private String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }

  private String normalizeReason(String reason, String fallback) {
    return reason == null || reason.isBlank() ? fallback : reason;
  }
}
