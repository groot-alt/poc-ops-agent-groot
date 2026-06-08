package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.IdentityClaimsMapper;
import com.company.opsagent.controlplane.modules.identity.api.IdentityAuthenticationService;
import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSession;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSessionState;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
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
 * 正式身份模块的登录认证服务。
 */
public class DefaultIdentityAuthenticationService implements IdentityAuthenticationService {

  private final AccountRepository accountRepository;
  private final PasswordCredentialRepository passwordCredentialRepository;
  private final PasswordVerifier passwordVerifier;
  private final IdentityClaimsMapper identityClaimsMapper;
  private final AccountSessionRepository accountSessionRepository;
  private final Clock clock;
  private final int lockoutThreshold;
  private final Duration lockoutDuration;
  private final Duration sessionIdleTimeout;
  private final Duration sessionAbsoluteTimeout;

  public DefaultIdentityAuthenticationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      PasswordVerifier passwordVerifier) {
    this(
        accountRepository,
        passwordCredentialRepository,
        null,
        passwordVerifier,
        Clock.systemUTC(),
        0,
        Duration.ofMinutes(15),
        Duration.ZERO,
        Duration.ZERO);
  }

  public DefaultIdentityAuthenticationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher passwordHasher,
      Clock clock,
      int lockoutThreshold,
      Duration lockoutDuration,
      Duration sessionIdleTimeout,
      Duration sessionAbsoluteTimeout) {
    this(
        accountRepository,
        passwordCredentialRepository,
        accountSessionRepository,
        (PasswordVerifier) passwordHasher,
        clock,
        lockoutThreshold,
        lockoutDuration,
        sessionIdleTimeout,
        sessionAbsoluteTimeout);
  }

  public DefaultIdentityAuthenticationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher passwordHasher,
      Clock clock,
      int lockoutThreshold,
      Duration sessionIdleTimeout,
      Duration sessionAbsoluteTimeout) {
    this(
        accountRepository,
        passwordCredentialRepository,
        accountSessionRepository,
        passwordHasher,
        clock,
        lockoutThreshold,
        Duration.ofMinutes(15),
        sessionIdleTimeout,
        sessionAbsoluteTimeout);
  }

  private DefaultIdentityAuthenticationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordVerifier passwordVerifier,
      Clock clock,
      int lockoutThreshold,
      Duration lockoutDuration,
      Duration sessionIdleTimeout,
      Duration sessionAbsoluteTimeout) {
    this.accountRepository = accountRepository;
    this.passwordCredentialRepository = passwordCredentialRepository;
    this.passwordVerifier = passwordVerifier;
    this.identityClaimsMapper = new IdentityClaimsMapper();
    this.accountSessionRepository = accountSessionRepository;
    this.clock = clock;
    this.lockoutThreshold = lockoutThreshold;
    this.lockoutDuration = lockoutDuration;
    this.sessionIdleTimeout = sessionIdleTimeout;
    this.sessionAbsoluteTimeout = sessionAbsoluteTimeout;
  }

  @Override
  public IdentityAuthenticationResult authenticate(PasswordLoginCommand command) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    Account account = accountRepository.findByUsername(command.username())
        .orElseThrow(this::invalidCredentials);

    if (account.status() == AccountStatus.DISABLED) {
      throw new IdentityAuthenticationException("ACCOUNT_DISABLED", "account is disabled");
    }
    if (account.status() == AccountStatus.LOCKED || account.isLockedAt(now)) {
      throw new IdentityAuthenticationException("ACCOUNT_LOCKED", "account is locked");
    }

    PasswordCredential credential = passwordCredentialRepository.findActiveByAccountId(account.accountId())
        .orElseThrow(this::invalidCredentials);
    if (!passwordVerifier.matches(command.password(), credential)) {
      recordFailedLogin(account, now);
      throw invalidCredentials();
    }

    accountRepository.save(new Account(
        account.accountId(),
        account.username(),
        account.status(),
        account.passwordState(),
        account.mfaRequirement(),
        account.roleCodes(),
        0,
        null));

    boolean passwordChangeRequired = account.requiresPasswordChange() || credential.mustChangeOnNextLogin();
    if (accountSessionRepository == null || sessionIdleTimeout.isZero() || sessionAbsoluteTimeout.isZero()) {
      return new IdentityAuthenticationResult(
          identityClaimsMapper.fromClaims(account.accountId(), account.username(), account.roleCodes()),
          passwordChangeRequired);
    }

    AccountSession session = new AccountSession(
        UUID.randomUUID().toString(),
        account.accountId(),
        AccountSessionState.ACTIVE,
        now.plus(sessionIdleTimeout),
        now.plus(sessionAbsoluteTimeout),
        null,
        passwordChangeRequired);
    accountSessionRepository.save(session);
    return new IdentityAuthenticationResult(
        identityClaimsMapper.fromClaims(account.accountId(), account.username(), account.roleCodes()),
        passwordChangeRequired,
        session.sessionId(),
        session.idleExpiresAt());
  }

  private void recordFailedLogin(Account account, OffsetDateTime now) {
    if (lockoutThreshold < 1) {
      return;
    }
    int nextFailureCount = account.failedLoginCount() + 1;
    boolean locked = nextFailureCount >= lockoutThreshold;
    accountRepository.save(new Account(
        account.accountId(),
        account.username(),
        locked ? AccountStatus.LOCKED : account.status(),
        account.passwordState(),
        account.mfaRequirement(),
        account.roleCodes(),
        nextFailureCount,
        locked ? now.plus(lockoutDuration) : account.lockedUntil()));
  }

  private IdentityAuthenticationException invalidCredentials() {
    return new IdentityAuthenticationException("INVALID_CREDENTIALS", "username or password is invalid");
  }
}
