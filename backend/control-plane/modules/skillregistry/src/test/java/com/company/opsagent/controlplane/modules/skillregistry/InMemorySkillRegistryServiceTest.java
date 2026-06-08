package com.company.opsagent.controlplane.modules.skillregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 内存注册中心查询测试。
 */
class InMemorySkillRegistryServiceTest {

  @Test
  void returnsLatestSkillVersionBySkillId() {
    InMemorySkillRegistryService service = new InMemorySkillRegistryService(List.of(
        registeredSkill("node-health-read", "1.0.0"),
        registeredSkill("node-health-read", "1.1.0"),
        registeredSkill("log-summary-read", "1.0.0")));

    assertTrue(service.findLatest("node-health-read").isPresent());
    assertEquals("1.1.0", service.findLatest("node-health-read").orElseThrow().descriptor().version());
    assertEquals(3, service.listSkills().size());
  }

  @Test
  void returnsSpecificVersionWhenRequested() {
    InMemorySkillRegistryService service = new InMemorySkillRegistryService(List.of(
        registeredSkill("node-health-read", "1.0.0"),
        registeredSkill("node-health-read", "1.1.0")));

    assertTrue(service.findByVersion("node-health-read", "1.0.0").isPresent());
    assertEquals("1.0.0", service.findByVersion("node-health-read", "1.0.0").orElseThrow().descriptor().version());
  }

  private RegisteredSkill registeredSkill(String skillId, String version) {
    return new RegisteredSkill(
        new SkillDescriptor(
            skillId,
            version,
            "示例 Skill",
            "用于测试注册中心的示例 Skill。",
            SkillCategory.INFRASTRUCTURE_DIAGNOSTICS,
            SkillRiskLevel.READ_ONLY,
            SkillExecutorType.SHELL,
            SkillOutputType.JSON,
            true,
            20,
            "platform-observability",
            List.of("ROLE_ops-reader"),
            List.of("test"),
            List.of(SkillInterceptorType.AUTHORIZATION, SkillInterceptorType.AUDIT),
            List.of()),
        new SkillPublicationMetadata(
            "platform-observability",
            OffsetDateTime.parse("2026-06-06T21:50:00+08:00"),
            "checksum",
            "HmacSHA256",
            "signature"),
        SkillPublicationStatus.VALIDATED,
        "skills/%s/%s/manifest.json".formatted(skillId, version));
  }
}
