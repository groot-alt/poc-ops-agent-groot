package com.company.opsagent.controlplane.bootstrap.config;

import com.company.opsagent.controlplane.bootstrap.security.localoidc.LocalOidcAuthorizationService;
import com.company.opsagent.controlplane.bootstrap.security.localoidc.LocalOidcKeyMaterial;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 本地模拟 OIDC Provider 的装配配置。
 */
@Configuration
@EnableConfigurationProperties(LocalOidcProviderProperties.class)
@ConditionalOnProperty(prefix = "ops-agent.local-oidc-provider", name = "enabled", havingValue = "true")
public class LocalOidcProviderConfiguration {

  @Bean
  LocalOidcKeyMaterial localOidcKeyMaterial() {
    return LocalOidcKeyMaterial.generate();
  }

  @Bean
  LocalOidcAuthorizationService localOidcAuthorizationService(LocalOidcProviderProperties properties) {
    return new LocalOidcAuthorizationService(properties.authorizationCodeTtl());
  }
}
