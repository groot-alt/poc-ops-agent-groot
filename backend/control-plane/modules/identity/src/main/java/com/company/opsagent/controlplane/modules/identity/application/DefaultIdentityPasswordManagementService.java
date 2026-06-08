package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.IdentityClaimsMapper;
import com.company.opsagent.controlplane.modules.identity.api.IdentityPasswordManagementService;
import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSession;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSessionState;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordState;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountSessionRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordCredentialRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordHasher;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordVerifier;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 正式身份模块的改密服务。
 */
public class DefaultIdentityPasswordManagementService implements IdentityPasswordManagementService {

  private final AccountRepository accountRepository;
  private final PasswordCredentialRepository passwordCredentialRepository;
  private final AccountSessionRepository accountSessionRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordVerifier passwordVerifier;
  private final Clock clock;
  private final Duration sessionIdleTimeout;
  private final Duration sessionAbsoluteTimeout;
  private final IdentityClaimsMapper identityClaimsMapper;

  public DefaultIdentityPasswordManagementService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher passwordHasher,
      Clock clock,
      Duration sessionIdleTimeout,
      Duration sessionAbsoluteTimeout) {
    this.accountRepository = accountRepository;
    this.passwordCredentialRepository = passwordCredentialRepository;
    this.accountSessionRepository = accountSessionRepository;
    this.passwordHasher = passwordHasher;
    this.passwordVerifier = (PasswordVerifier) passwordHasher;
    this.clock = clock;
    this.sessionIdleTimeout = sessionIdleTimeout;
    this.sessionAbsoluteTimeout = sessionAbsoluteTimeout;
    this.identityClaimsMapper = new IdentityClaimsMapper();
  }

  @Override
  public IdentityAuthenticationResult changePassword(String sessionId, PasswordChangeCommand command) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    AccountSession session = accountSessionRepository.findBySessionId(sessionId)
        .filter(found -> found.isActiveAt(now))
        .orElseThrow(() -> new IdentityAuthenticationException("SESSION_NOT_FOUND", "session not found"));
    Account account = accountRepository.findByAccountId(session.accountId())
        .orElseThrow(() -> new IdentityAuthenticationException("ACCOUNT_NOT_FOUND", "account not found"));
    PasswordCredential currentCredential = passwordCredentialRepository.findActiveByAccountId(account.accountId())
        .orElseThrow(() -> new IdentityAuthenticationException("CREDENTIAL_NOT_FOUND", "password credential missing"));
    if (!passwordVerifier.matches(command.currentPassword(), currentCredential)) {
      throw new IdentityAuthenticationException("INVALID_CREDENTIALS", "current password is invalid");
    }

    passwordCredentialRepository.save(passwordHasher.hash(
        account.accountId(),
        command.newPassword(),
        currentCredential.passwordVersion() + 1,
        false));
    accountRepository.save(new Account(
        account.accountId(),
        account.username(),
        AccountStatus.ACTIVE,
        PasswordState.ACTIVE,
        account.mfaRequirement(),
        account.roleCodes(),
        0,
        null));
    accountSessionRepository.revokeByAccountId(account.accountId(), now, "PASSWORD_CHANGED");

    AccountSession freshSession = new AccountSession(
        UUID.randomUUID().toString(),
        account.accountId(),
        AccountSessionState.ACTIVE,
        now.plus(sessionIdleTimeout),
        now.plus(sessionAbsoluteTimeout),
        null,
        false);
    accountSessionRepository.save(freshSession);
    return new IdentityAuthenticationResult(
        identityClaimsMapper.fromClaims(account.accountId(), account.username(), account.roleCodes()),
        false,
        freshSession.sessionId(),
        freshSession.idleExpiresAt());
  }
}
