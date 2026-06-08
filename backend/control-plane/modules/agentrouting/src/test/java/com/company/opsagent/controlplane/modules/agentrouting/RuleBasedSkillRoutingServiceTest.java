package com.company.opsagent.controlplane.modules.agentrouting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.company.opsagent.controlplane.modules.skillregistry.RegisteredSkill;
import com.company.opsagent.controlplane.modules.skillregistry.SkillCategory;
import com.company.opsagent.controlplane.modules.skillregistry.SkillDescriptor;
import com.company.opsagent.controlplane.modules.skillregistry.SkillExecutorType;
import com.company.opsagent.controlplane.modules.skillregistry.SkillInterceptorType;
import com.company.opsagent.controlplane.modules.skillregistry.SkillOutputType;
import com.company.opsagent.controlplane.modules.skillregistry.SkillParameterDescriptor;
import com.company.opsagent.controlplane.modules.skillregistry.SkillParameterType;
import com.company.opsagent.controlplane.modules.skillregistry.SkillPublicationMetadata;
import com.company.opsagent.controlplane.modules.skillregistry.SkillPublicationStatus;
import com.company.opsagent.controlplane.modules.skillregistry.SkillReleaseManagementService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillReleaseSnapshot;
import com.company.opsagent.controlplane.modules.skillregistry.SkillReleaseStage;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRegistryService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRiskLevel;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 路由规则测试。
 */
class RuleBasedSkillRoutingServiceTest {

  @Test
  void filtersByCategoryRiskParameterAndPublicationStatus() {
    SkillRegistryService registryService = new StubSkillRegistryService(List.of(
        skill("node-health-read", SkillCategory.INFRASTRUCTURE_DIAGNOSTICS, List.of("nodeName"), SkillPublicationStatus.VALIDATED),
        skill("application-log-summary-read", SkillCategory.APPLICATION_DIAGNOSTICS, List.of("applicationName", "minutes"), SkillPublicationStatus.VALIDATED),
        skill("draft-log-summary-read", SkillCategory.APPLICATION_DIAGNOSTICS, List.of("applicationName"), SkillPublicationStatus.DRAFT)));

    SkillReleaseManagementService releaseManagementService = new StubSkillReleaseManagementService();

    RuleBasedSkillRoutingService routingService = new RuleBasedSkillRoutingService(registryService, releaseManagementService);

    List<SkillRouteCandidate> candidates = routingService.findCandidates(new SkillRoutingCriteria(
        null,
        SkillCategory.APPLICATION_DIAGNOSTICS,
        SkillRiskLevel.READ_ONLY,
        List.of("applicationName"),
        List.of("summary"),
        List.of(),
        SkillPublicationStatus.VALIDATED));

    assertEquals(1, candidates.size());
    assertEquals("application-log-summary-read", candidates.get(0).skill().descriptor().skillId());
    assertEquals(SkillReleaseStage.GENERAL_AVAILABLE, candidates.get(0).releaseSnapshot().stage());
  }

  private RegisteredSkill skill(
      String skillId,
      SkillCategory category,
      List<String> parameterNames,
      SkillPublicationStatus publicationStatus) {
    return new RegisteredSkill(
        new SkillDescriptor(
            skillId,
            "1.0.0",
            "示例 Skill",
            "用于测试路由的 Skill。",
            category,
            SkillRiskLevel.READ_ONLY,
            SkillExecutorType.SHELL,
            SkillOutputType.JSON,
            true,
            20,
            "platform-observability",
            List.of("ROLE_ops-reader"),
            List.of("summary"),
            List.of(SkillInterceptorType.AUTHORIZATION, SkillInterceptorType.AUDIT),
            parameterNames.stream()
                .map(parameter -> new SkillParameterDescriptor(
                    parameter,
                    parameter,
                    parameter,
                    SkillParameterType.STRING,
                    true,
                    List.of(),
                    null))
                .toList()),
        new SkillPublicationMetadata(
            "platform-observability",
            OffsetDateTime.parse("2026-06-06T22:20:00+08:00"),
            "checksum",
            "HmacSHA256",
            "signature"),
        publicationStatus,
        "skills/%s/manifest.json".formatted(skillId));
  }

  private record StubSkillRegistryService(List<RegisteredSkill> skills) implements SkillRegistryService {
    @Override
    public List<RegisteredSkill> listSkills() {
      return skills;
    }

    @Override
    public Optional<RegisteredSkill> findLatest(String skillId) {
      return skills.stream().filter(skill -> skill.descriptor().skillId().equals(skillId)).findFirst();
    }

    @Override
    public Optional<RegisteredSkill> findByVersion(String skillId, String version) {
      return skills.stream()
          .filter(skill -> skill.descriptor().skillId().equals(skillId) && skill.descriptor().version().equals(version))
          .findFirst();
    }
  }

  /**
   * 为路由测试提供固定发布态，避免测试依赖发布管理模块的可变状态。
   */
  private static final class StubSkillReleaseManagementService implements SkillReleaseManagementService {

    @Override
    public SkillReleaseSnapshot snapshot(RegisteredSkill registeredSkill) {
      if ("draft-log-summary-read".equals(registeredSkill.descriptor().skillId())) {
        return snapshot(registeredSkill.descriptor().skillId(), registeredSkill.descriptor().version(),
            SkillReleaseStage.CANARY, 10, List.of("ops-canary"), "灰度中");
      }
      return snapshot(registeredSkill.descriptor().skillId(), registeredSkill.descriptor().version(),
          SkillReleaseStage.GENERAL_AVAILABLE, 100, List.of(), "全量中");
    }

    @Override
    public SkillReleaseSnapshot promoteCanary(
        String skillId,
        String version,
        int rolloutPercentage,
        List<String> targetContextTags,
        String reason) {
      return snapshot(skillId, version, SkillReleaseStage.CANARY, rolloutPercentage, targetContextTags, reason);
    }

    @Override
    public SkillReleaseSnapshot promoteGeneral(String skillId, String version, String reason) {
      return snapshot(skillId, version, SkillReleaseStage.GENERAL_AVAILABLE, 100, List.of(), reason);
    }

    @Override
    public SkillReleaseSnapshot rollback(String skillId, String version, String reason) {
      return snapshot(skillId, version, SkillReleaseStage.ROLLED_BACK, 0, List.of(), reason);
    }

    private SkillReleaseSnapshot snapshot(
        String skillId,
        String version,
        SkillReleaseStage stage,
        int rolloutPercentage,
        List<String> targetContextTags,
        String reason) {
      return new SkillReleaseSnapshot(
          skillId,
          version,
          stage,
          rolloutPercentage,
          targetContextTags,
          reason,
          OffsetDateTime.parse("2026-06-06T22:21:00+08:00"));
    }
  }
}
