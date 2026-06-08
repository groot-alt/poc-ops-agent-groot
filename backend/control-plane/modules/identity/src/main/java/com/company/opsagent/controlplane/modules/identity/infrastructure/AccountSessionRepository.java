package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.AccountSession;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 会话事实源访问接口。
 */
public interface AccountSessionRepository {

  Optional<AccountSession> findBySessionId(String sessionId);

  default void save(AccountSession session) {
    throw new UnsupportedOperationException("save is not implemented");
  }

  default void revokeBySessionId(String sessionId, OffsetDateTime revokedAt, String reason) {
    throw new UnsupportedOperationException("revokeBySessionId is not implemented");
  }

  default void revokeByAccountId(String accountId, OffsetDateTime revokedAt, String reason) {
    throw new UnsupportedOperationException("revokeByAccountId is not implemented");
  }

  default void touch(String sessionId, OffsetDateTime refreshedIdleExpiresAt) {
    throw new UnsupportedOperationException("touch is not implemented");
  }
}
