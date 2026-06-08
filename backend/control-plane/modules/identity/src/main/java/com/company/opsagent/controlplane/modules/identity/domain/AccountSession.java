package com.company.opsagent.controlplane.modules.identity.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 正式身份模块的浏览器会话领域对象。
 */
public record AccountSession(
    String sessionId,
    String accountId,
    AccountSessionState state,
    OffsetDateTime idleExpiresAt,
    OffsetDateTime absoluteExpiresAt,
    OffsetDateTime revokedAt,
    boolean passwordChangeRequired) {

  public AccountSession {
    sessionId = requiredText(sessionId, "sessionId");
    accountId = requiredText(accountId, "accountId");
    state = Objects.requireNonNull(state, "state must not be null");
    idleExpiresAt = Objects.requireNonNull(idleExpiresAt, "idleExpiresAt must not be null");
    absoluteExpiresAt = Objects.requireNonNull(absoluteExpiresAt, "absoluteExpiresAt must not be null");
  }

  public boolean isActiveAt(OffsetDateTime now) {
    OffsetDateTime currentTime = Objects.requireNonNull(now, "now must not be null");
    return state == AccountSessionState.ACTIVE
        && revokedAt == null
        && !idleExpiresAt.isBefore(currentTime)
        && !absoluteExpiresAt.isBefore(currentTime);
  }

  public AccountSession revoke(OffsetDateTime revokedAt) {
    return new AccountSession(
        sessionId,
        accountId,
        AccountSessionState.REVOKED,
        idleExpiresAt,
        absoluteExpiresAt,
        revokedAt,
        passwordChangeRequired);
  }

  public AccountSession touch(OffsetDateTime refreshedIdleExpiresAt) {
    return new AccountSession(
        sessionId,
        accountId,
        state,
        refreshedIdleExpiresAt,
        absoluteExpiresAt,
        revokedAt,
        passwordChangeRequired);
  }

  private static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
