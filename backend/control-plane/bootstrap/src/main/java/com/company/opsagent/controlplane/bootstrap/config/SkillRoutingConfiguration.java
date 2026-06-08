package com.company.opsagent.controlplane.bootstrap.config;

import com.company.opsagent.controlplane.modules.agentrouting.RuleBasedSkillRoutingService;
import com.company.opsagent.controlplane.modules.agentrouting.SkillRoutingService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillReleaseManagementService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Skill 路由装配配置。
 */
@Configuration
public class SkillRoutingConfiguration {

  /**
   * 构建基于规则的 Skill 路由服务。
   */
  @Bean
  SkillRoutingService skillRoutingService(
      SkillRegistryService skillRegistryService,
      SkillReleaseManagementService skillReleaseManagementService) {
    return new RuleBasedSkillRoutingService(skillRegistryService, skillReleaseManagementService);
  }
}
