package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.api.IdentitySessionManagementService;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountSessionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * 正式身份模块的会话撤销服务。
 */
public class DefaultIdentitySessionManagementService implements IdentitySessionManagementService {

  private final AccountSessionRepository accountSessionRepository;
  private final Clock clock;

  public DefaultIdentitySessionManagementService(
      AccountSessionRepository accountSessionRepository,
      Clock clock) {
    this.accountSessionRepository = accountSessionRepository;
    this.clock = clock;
  }

  @Override
  public void logout(String sessionId) {
    accountSessionRepository.revokeBySessionId(sessionId, OffsetDateTime.now(clock), "LOGOUT");
  }
}
