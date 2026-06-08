package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 浏览器会话状态快照。
 */
public record IdentitySessionStatus(
    boolean authenticated,
    OperatorIdentity identity,
    OffsetDateTime sessionExpiresAt,
    boolean passwordChangeRequired,
    String authenticationType) {

  public IdentitySessionStatus {
    authenticationType = Objects.requireNonNull(authenticationType, "authenticationType must not be null");
    if (authenticated) {
      identity = Objects.requireNonNull(identity, "identity must not be null");
      sessionExpiresAt = Objects.requireNonNull(sessionExpiresAt, "sessionExpiresAt must not be null");
    }
  }
}
