package com.company.opsagent.controlplane.modules.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSession;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSessionState;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.domain.MfaRequirement;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordState;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountSessionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 验证正式身份模块的会话查询边界。
 */
class DefaultIdentitySessionQueryServiceTest {

  @Test
  void resolvesIdentityFromActiveSession() {
    DefaultIdentitySessionQueryService service = new DefaultIdentitySessionQueryService(
        accountRepository(activeAccount()),
        sessionRepository(new AccountSession(
            "session-1",
            "account-1",
            AccountSessionState.ACTIVE,
            OffsetDateTime.now().plusMinutes(15),
            OffsetDateTime.now().plusHours(8),
            null,
            false)));

    Optional<OperatorIdentity> result = service.findOperatorIdentityBySessionId("session-1");

    assertTrue(result.isPresent());
    assertEquals("account-1", result.orElseThrow().subject());
  }

  @Test
  void returnsEmptyForRevokedSession() {
    DefaultIdentitySessionQueryService service = new DefaultIdentitySessionQueryService(
        accountRepository(activeAccount()),
        sessionRepository(new AccountSession(
            "session-1",
            "account-1",
            AccountSessionState.REVOKED,
            OffsetDateTime.now().plusMinutes(15),
            OffsetDateTime.now().plusHours(8),
            OffsetDateTime.now(),
            false)));

    assertTrue(service.findOperatorIdentityBySessionId("session-1").isEmpty());
  }

  @Test
  void returnsEmptyForExpiredSession() {
    DefaultIdentitySessionQueryService service = new DefaultIdentitySessionQueryService(
        accountRepository(activeAccount()),
        sessionRepository(new AccountSession(
            "session-1",
            "account-1",
            AccountSessionState.ACTIVE,
            OffsetDateTime.now().minusMinutes(1),
            OffsetDateTime.now().plusHours(8),
            null,
            false)));

    assertTrue(service.findOperatorIdentityBySessionId("session-1").isEmpty());
  }

  private Account activeAccount() {
    return new Account(
        "account-1",
        "alice",
        AccountStatus.ACTIVE,
        PasswordState.ACTIVE,
        MfaRequirement.NOT_REQUIRED,
        List.of("ROLE_ops-reader"),
        0,
        null);
  }

  private AccountRepository accountRepository(Account account) {
    return new AccountRepository() {
      @Override
      public Optional<Account> findByUsername(String username) {
        return Optional.of(account);
      }

      @Override
      public Optional<Account> findByAccountId(String accountId) {
        return Optional.of(account);
      }
    };
  }

  private AccountSessionRepository sessionRepository(AccountSession session) {
    return sessionId -> Optional.of(session);
  }
}
