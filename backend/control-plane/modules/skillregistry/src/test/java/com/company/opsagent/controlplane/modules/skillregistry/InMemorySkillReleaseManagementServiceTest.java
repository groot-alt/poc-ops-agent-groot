package com.company.opsagent.controlplane.modules.skillregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 发布态管理测试。
 */
class InMemorySkillReleaseManagementServiceTest {

  @Test
  void promotesAndRollsBackSkillReleaseState() {
    RegisteredSkill skill = new RegisteredSkill(
        new SkillDescriptor(
            "node-health-read",
            "1.1.0",
            "节点健康检查",
            "读取节点状态。",
            SkillCategory.INFRASTRUCTURE_DIAGNOSTICS,
            SkillRiskLevel.READ_ONLY,
            SkillExecutorType.SHELL,
            SkillOutputType.JSON,
            true,
            20,
            "platform-observability",
            List.of("ROLE_ops-reader"),
            List.of("health"),
            List.of(SkillInterceptorType.AUTHORIZATION),
            List.of()),
        new SkillPublicationMetadata(
            "platform-observability",
            OffsetDateTime.parse("2026-06-06T22:30:00+08:00"),
            "checksum",
            "HmacSHA256",
            "signature"),
        SkillPublicationStatus.VALIDATED,
        "node-health/manifest.json");

    SkillRegistryService registryService = new SkillRegistryService() {
      @Override
      public List<RegisteredSkill> listSkills() {
        return List.of(skill);
      }

      @Override
      public Optional<RegisteredSkill> findLatest(String skillId) {
        return Optional.of(skill);
      }

      @Override
      public Optional<RegisteredSkill> findByVersion(String skillId, String version) {
        return Optional.of(skill);
      }
    };

    InMemorySkillReleaseManagementService service = new InMemorySkillReleaseManagementService(registryService);

    assertEquals(SkillReleaseStage.GENERAL_AVAILABLE, service.snapshot(skill).stage());
    assertEquals(SkillReleaseStage.CANARY,
        service.promoteCanary("node-health-read", "1.1.0", 20, List.of("ops-canary"), "灰度发布").stage());
    assertEquals(SkillReleaseStage.ROLLED_BACK,
        service.rollback("node-health-read", "1.1.0", "发现问题回滚").stage());
  }
}
