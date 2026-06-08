package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 登录认证结果。
 */
public record IdentityAuthenticationResult(
    OperatorIdentity identity,
    boolean passwordChangeRequired,
    String sessionId,
    OffsetDateTime sessionExpiresAt) {

  public IdentityAuthenticationResult {
    identity = Objects.requireNonNull(identity, "identity must not be null");
  }

  public IdentityAuthenticationResult(OperatorIdentity identity, boolean passwordChangeRequired) {
    this(identity, passwordChangeRequired, null, null);
  }
}
