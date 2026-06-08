package com.company.opsagent.controlplane.bootstrap.config;

import com.company.opsagent.controlplane.modules.identity.api.IdentityAdministrationService;
import com.company.opsagent.controlplane.modules.identity.api.IdentityAuthenticationService;
import com.company.opsagent.controlplane.modules.identity.api.IdentityPasswordManagementService;
import com.company.opsagent.controlplane.modules.identity.api.IdentitySessionManagementService;
import com.company.opsagent.controlplane.modules.identity.api.IdentitySessionQueryService;
import com.company.opsagent.controlplane.modules.identity.application.DefaultIdentityAdministrationService;
import com.company.opsagent.controlplane.modules.identity.application.DefaultIdentityAuthenticationService;
import com.company.opsagent.controlplane.modules.identity.application.DefaultIdentityPasswordManagementService;
import com.company.opsagent.controlplane.modules.identity.application.DefaultIdentitySessionManagementService;
import com.company.opsagent.controlplane.modules.identity.application.DefaultIdentitySessionQueryService;
import com.company.opsagent.controlplane.modules.identity.application.Pbkdf2PasswordService;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountSessionRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordCredentialRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordHasher;
import com.company.opsagent.controlplane.modules.identity.infrastructure.R2dbcAccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.R2dbcAccountSessionRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.R2dbcPasswordCredentialRepository;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * 正式内建身份模式装配。
 */
@Configuration
@ConditionalOnProperty(prefix = "ops-agent.security", name = "auth-mode", havingValue = "built-in")
@EnableConfigurationProperties(BuiltInIdentityProperties.class)
public class BuiltInIdentityConfiguration {

  @Bean
  @ConditionalOnMissingBean(Clock.class)
  Clock builtInIdentityClock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "ops-agent.built-in-identity",
      name = "schema-initializer-enabled",
      havingValue = "true",
      matchIfMissing = true)
  ConnectionFactoryInitializer identitySchemaInitializer(ConnectionFactory connectionFactory) {
    var initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);
    initializer.setDatabasePopulator(new ResourceDatabasePopulator(
        new ClassPathResource("sql/migrations/V001__identity_schema.sql")));
    return initializer;
  }

  @Bean
  @ConditionalOnMissingBean(AccountRepository.class)
  AccountRepository builtInAccountRepository(DatabaseClient databaseClient, Clock builtInIdentityClock) {
    return new R2dbcAccountRepository(databaseClient, builtInIdentityClock);
  }

  @Bean
  @ConditionalOnMissingBean(PasswordCredentialRepository.class)
  PasswordCredentialRepository builtInPasswordCredentialRepository(
      DatabaseClient databaseClient,
      Clock builtInIdentityClock) {
    return new R2dbcPasswordCredentialRepository(databaseClient, builtInIdentityClock);
  }

  @Bean
  @ConditionalOnMissingBean(AccountSessionRepository.class)
  AccountSessionRepository builtInAccountSessionRepository(
      DatabaseClient databaseClient,
      Clock builtInIdentityClock) {
    return new R2dbcAccountSessionRepository(databaseClient, builtInIdentityClock);
  }

  @Bean
  @ConditionalOnMissingBean(PasswordHasher.class)
  PasswordHasher builtInPasswordHasher(Clock builtInIdentityClock) {
    return new Pbkdf2PasswordService(builtInIdentityClock);
  }

  @Bean
  @ConditionalOnMissingBean(IdentityAuthenticationService.class)
  IdentityAuthenticationService identityAuthenticationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher builtInPasswordHasher,
      Clock builtInIdentityClock,
      BuiltInIdentityProperties properties) {
    return new DefaultIdentityAuthenticationService(
        accountRepository,
        passwordCredentialRepository,
        accountSessionRepository,
        builtInPasswordHasher,
        builtInIdentityClock,
        properties.getLockoutThreshold(),
        properties.getLockoutDuration(),
        properties.getSessionIdleTimeout(),
        properties.getSessionAbsoluteTimeout());
  }

  @Bean
  @ConditionalOnMissingBean(IdentitySessionQueryService.class)
  IdentitySessionQueryService identitySessionQueryService(
      AccountRepository accountRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      Clock builtInIdentityClock,
      BuiltInIdentityProperties properties) {
    return new DefaultIdentitySessionQueryService(
        accountRepository,
        accountSessionRepository,
        passwordCredentialRepository,
        builtInIdentityClock,
        properties.getSessionIdleTimeout());
  }

  @Bean
  @ConditionalOnMissingBean(IdentityPasswordManagementService.class)
  IdentityPasswordManagementService identityPasswordManagementService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher builtInPasswordHasher,
      Clock builtInIdentityClock,
      BuiltInIdentityProperties properties) {
    return new DefaultIdentityPasswordManagementService(
        accountRepository,
        passwordCredentialRepository,
        accountSessionRepository,
        builtInPasswordHasher,
        builtInIdentityClock,
        properties.getSessionIdleTimeout(),
        properties.getSessionAbsoluteTimeout());
  }

  @Bean
  @ConditionalOnMissingBean(IdentityAdministrationService.class)
  IdentityAdministrationService identityAdministrationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher builtInPasswordHasher,
      Clock builtInIdentityClock) {
    return new DefaultIdentityAdministrationService(
        accountRepository,
        passwordCredentialRepository,
        accountSessionRepository,
        builtInPasswordHasher,
        builtInIdentityClock);
  }

  @Bean
  @ConditionalOnMissingBean(IdentitySessionManagementService.class)
  IdentitySessionManagementService identitySessionManagementService(
      AccountSessionRepository accountSessionRepository,
      Clock builtInIdentityClock) {
    return new DefaultIdentitySessionManagementService(accountSessionRepository, builtInIdentityClock);
  }
}
