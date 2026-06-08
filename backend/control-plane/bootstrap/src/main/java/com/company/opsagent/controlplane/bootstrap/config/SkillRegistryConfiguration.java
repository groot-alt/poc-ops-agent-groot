package com.company.opsagent.controlplane.bootstrap.config;

import com.company.opsagent.controlplane.modules.skillregistry.FileSystemSkillManifestLoader;
import com.company.opsagent.controlplane.modules.skillregistry.FileSystemSkillPublicationWorkflowService;
import com.company.opsagent.controlplane.modules.skillregistry.InMemorySkillReleaseManagementService;
import com.company.opsagent.controlplane.modules.skillregistry.InMemorySkillRegistryService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillManifestLoader;
import com.company.opsagent.controlplane.modules.skillregistry.SkillPublicationWorkflowService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillReleaseManagementService;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRegistryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Skill 注册中心装配配置。
 */
@Configuration
@EnableConfigurationProperties(SkillRegistryProperties.class)
public class SkillRegistryConfiguration {

  /**
   * 构建本地文件系统 Manifest 加载器。
   */
  @Bean
  SkillManifestLoader skillManifestLoader(
      SkillRegistryProperties properties,
      ObjectMapper objectMapper) {
    return new FileSystemSkillManifestLoader(
        Path.of(properties.getRootPath()),
        objectMapper,
        properties.isSignatureRequired(),
        properties.getSigningSecret());
  }

  /**
   * 构建显式发布校验服务。
   */
  @Bean
  SkillPublicationWorkflowService skillPublicationWorkflowService(SkillManifestLoader skillManifestLoader) {
    return new FileSystemSkillPublicationWorkflowService((FileSystemSkillManifestLoader) skillManifestLoader);
  }

  /**
   * 构建启动期一次性加载的内存注册中心。
   */
  @Bean
  SkillRegistryService skillRegistryService(SkillManifestLoader skillManifestLoader) {
    return new InMemorySkillRegistryService(skillManifestLoader.loadAll());
  }

  /**
   * 构建 P1 使用的内存发布态管理服务，为路由提供灰度和回滚状态。
   */
  @Bean
  SkillReleaseManagementService skillReleaseManagementService(SkillRegistryService skillRegistryService) {
    return new InMemorySkillReleaseManagementService(skillRegistryService);
  }
}
