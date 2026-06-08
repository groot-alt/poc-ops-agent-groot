package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.IdentityClaimsMapper;
import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import com.company.opsagent.controlplane.modules.identity.api.IdentitySessionQueryService;
import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSession;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordCredentialRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 正式身份模块的默认会话查询服务。
 */
public class DefaultIdentitySessionQueryService implements IdentitySessionQueryService {

  private final AccountRepository accountRepository;
  private final AccountSessionRepository accountSessionRepository;
  private final PasswordCredentialRepository passwordCredentialRepository;
  private final IdentityClaimsMapper identityClaimsMapper;
  private final Clock clock;
  private final Duration sessionIdleTimeout;

  public DefaultIdentitySessionQueryService(
      AccountRepository accountRepository,
      AccountSessionRepository accountSessionRepository) {
    this(accountRepository, accountSessionRepository, null, Clock.systemUTC());
  }

  public DefaultIdentitySessionQueryService(
      AccountRepository accountRepository,
      AccountSessionRepository accountSessionRepository,
      Clock clock) {
    this(accountRepository, accountSessionRepository, null, clock);
  }

  public DefaultIdentitySessionQueryService(
      AccountRepository accountRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      Clock clock) {
    this(accountRepository, accountSessionRepository, passwordCredentialRepository, clock, Duration.ZERO);
  }

  public DefaultIdentitySessionQueryService(
      AccountRepository accountRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      Clock clock,
      Duration sessionIdleTimeout) {
    this.accountRepository = accountRepository;
    this.accountSessionRepository = accountSessionRepository;
    this.passwordCredentialRepository = passwordCredentialRepository;
    this.identityClaimsMapper = new IdentityClaimsMapper();
    this.clock = clock;
    this.sessionIdleTimeout = sessionIdleTimeout == null ? Duration.ZERO : sessionIdleTimeout;
  }

  @Override
  public Optional<OperatorIdentity> findOperatorIdentityBySessionId(String sessionId) {
    return loadActiveSession(sessionId)
        .flatMap(session -> accountRepository.findByAccountId(session.accountId())
            .filter(account -> isAccountUsable(account) && !requiresPasswordChange(account))
            .map(account -> identityClaimsMapper.fromClaims(
                account.accountId(),
                account.username(),
                account.roleCodes())));
  }

  @Override
  public Optional<IdentitySessionStatus> findSessionStatusBySessionId(String sessionId) {
    return loadActiveSession(sessionId)
        .flatMap(session -> accountRepository.findByAccountId(session.accountId())
            .map(account -> new IdentitySessionStatus(
                true,
                identityClaimsMapper.fromClaims(account.accountId(), account.username(), account.roleCodes()),
                session.idleExpiresAt(),
                requiresPasswordChange(account),
                "PASSWORD")));
  }

  private Optional<AccountSession> loadActiveSession(String sessionId) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    return accountSessionRepository.findBySessionId(sessionId)
        .filter(session -> session.isActiveAt(now))
        .map(session -> refreshSession(session, now));
  }

  private AccountSession refreshSession(AccountSession session, OffsetDateTime now) {
    if (sessionIdleTimeout.isZero() || sessionIdleTimeout.isNegative()) {
      return session;
    }
    OffsetDateTime refreshedIdleExpiresAt = now.plus(sessionIdleTimeout);
    if (refreshedIdleExpiresAt.isAfter(session.absoluteExpiresAt())) {
      refreshedIdleExpiresAt = session.absoluteExpiresAt();
    }
    if (!refreshedIdleExpiresAt.isAfter(session.idleExpiresAt())) {
      return session;
    }
    accountSessionRepository.touch(session.sessionId(), refreshedIdleExpiresAt);
    return session.touch(refreshedIdleExpiresAt);
  }

  private boolean isAccountUsable(Account account) {
    return account.status() == AccountStatus.ACTIVE
        || account.status() == AccountStatus.PASSWORD_RESET_REQUIRED;
  }

  private boolean requiresPasswordChange(Account account) {
    if (account.requiresPasswordChange()) {
      return true;
    }
    if (passwordCredentialRepository == null) {
      return false;
    }
    return passwordCredentialRepository.findActiveByAccountId(account.accountId())
        .map(credential -> credential.mustChangeOnNextLogin())
        .orElse(false);
  }
}
