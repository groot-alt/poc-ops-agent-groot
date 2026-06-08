package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.AccountSession;
import com.company.opsagent.controlplane.modules.identity.domain.AccountSessionState;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;

/**
 * 基于 R2DBC 的浏览器会话事实源实现。
 */
public class R2dbcAccountSessionRepository implements AccountSessionRepository {

  private final DatabaseClient databaseClient;
  private final Clock clock;

  public R2dbcAccountSessionRepository(DatabaseClient databaseClient, Clock clock) {
    this.databaseClient = databaseClient;
    this.clock = clock;
  }

  @Override
  public Optional<AccountSession> findBySessionId(String sessionId) {
    return databaseClient.sql("""
            select session_id,
                   account_id,
                   session_state,
                   expires_at,
                   absolute_expires_at,
                   revoked_at,
                   password_change_required
            from identity_account_session
            where session_id = :sessionId
            """)
        .bind("sessionId", sessionId)
        .map((row, metadata) -> new AccountSession(
            row.get("session_id", String.class),
            row.get("account_id", String.class),
            AccountSessionState.valueOf(row.get("session_state", String.class)),
            row.get("expires_at", OffsetDateTime.class),
            row.get("absolute_expires_at", OffsetDateTime.class),
            row.get("revoked_at", OffsetDateTime.class),
            Boolean.TRUE.equals(row.get("password_change_required", Boolean.class))))
        .one()
        .blockOptional();
  }

  @Override
  public void save(AccountSession session) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    GenericExecuteSpec updateSpec = databaseClient.sql("""
            update identity_account_session
            set account_id = :accountId,
                session_state = :sessionState,
                last_seen_at = :lastSeenAt,
                expires_at = :expiresAt,
                absolute_expires_at = :absoluteExpiresAt,
                revoked_at = :revokedAt,
                revoked_reason = :revokedReason,
                password_change_required = :passwordChangeRequired
            where session_id = :sessionId
            """)
        .bind("sessionId", session.sessionId())
        .bind("accountId", session.accountId())
        .bind("sessionState", session.state().name())
        .bind("lastSeenAt", now)
        .bind("expiresAt", session.idleExpiresAt())
        .bind("absoluteExpiresAt", session.absoluteExpiresAt())
        .bind("passwordChangeRequired", session.passwordChangeRequired());
    updateSpec = bindNullable(updateSpec, "revokedAt", session.revokedAt(), OffsetDateTime.class);
    updateSpec = updateSpec.bindNull("revokedReason", String.class);
    Long updatedRows = updateSpec.fetch()
        .rowsUpdated()
        .block();
    if (updatedRows != null && updatedRows > 0) {
      return;
    }

    GenericExecuteSpec insertSpec = databaseClient.sql("""
            insert into identity_account_session (
              session_id,
              account_id,
              session_state,
              issued_at,
              last_seen_at,
              expires_at,
              absolute_expires_at,
              revoked_at,
              revoked_reason,
              password_change_required
            ) values (
              :sessionId,
              :accountId,
              :sessionState,
              :issuedAt,
              :lastSeenAt,
              :expiresAt,
              :absoluteExpiresAt,
              :revokedAt,
              :revokedReason,
              :passwordChangeRequired
            )
            """)
        .bind("sessionId", session.sessionId())
        .bind("accountId", session.accountId())
        .bind("sessionState", session.state().name())
        .bind("issuedAt", now)
        .bind("lastSeenAt", now)
        .bind("expiresAt", session.idleExpiresAt())
        .bind("absoluteExpiresAt", session.absoluteExpiresAt())
        .bind("passwordChangeRequired", session.passwordChangeRequired());
    insertSpec = bindNullable(insertSpec, "revokedAt", session.revokedAt(), OffsetDateTime.class);
    insertSpec = insertSpec.bindNull("revokedReason", String.class);
    insertSpec.fetch()
        .rowsUpdated()
        .block();
  }

  @Override
  public void revokeBySessionId(String sessionId, OffsetDateTime revokedAt, String reason) {
    databaseClient.sql("""
            update identity_account_session
            set session_state = :sessionState,
                revoked_at = :revokedAt,
                revoked_reason = :revokedReason
            where session_id = :sessionId
              and revoked_at is null
            """)
        .bind("sessionState", AccountSessionState.REVOKED.name())
        .bind("revokedAt", revokedAt)
        .bind("revokedReason", reason)
        .bind("sessionId", sessionId)
        .fetch()
        .rowsUpdated()
        .block();
  }

  @Override
  public void revokeByAccountId(String accountId, OffsetDateTime revokedAt, String reason) {
    databaseClient.sql("""
            update identity_account_session
            set session_state = :sessionState,
                revoked_at = :revokedAt,
                revoked_reason = :revokedReason
            where account_id = :accountId
              and revoked_at is null
            """)
        .bind("sessionState", AccountSessionState.REVOKED.name())
        .bind("revokedAt", revokedAt)
        .bind("revokedReason", reason)
        .bind("accountId", accountId)
        .fetch()
        .rowsUpdated()
        .block();
  }

  @Override
  public void touch(String sessionId, OffsetDateTime refreshedIdleExpiresAt) {
    databaseClient.sql("""
            update identity_account_session
            set last_seen_at = :lastSeenAt,
                expires_at = :expiresAt
            where session_id = :sessionId
              and session_state = :sessionState
              and revoked_at is null
            """)
        .bind("lastSeenAt", OffsetDateTime.now(clock))
        .bind("expiresAt", refreshedIdleExpiresAt)
        .bind("sessionId", sessionId)
        .bind("sessionState", AccountSessionState.ACTIVE.name())
        .fetch()
        .rowsUpdated()
        .block();
  }

  private GenericExecuteSpec bindNullable(
      GenericExecuteSpec spec,
      String name,
      Object value,
      Class<?> type) {
    return value == null ? spec.bindNull(name, type) : spec.bind(name, value);
  }
}
