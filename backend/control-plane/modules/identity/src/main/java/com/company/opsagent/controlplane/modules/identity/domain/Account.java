package com.company.opsagent.controlplane.modules.identity.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 正式身份模块的账号领域对象。
 */
public record Account(
    String accountId,
    String username,
    AccountStatus status,
    PasswordState passwordState,
    MfaRequirement mfaRequirement,
    List<String> roleCodes,
    int failedLoginCount,
    OffsetDateTime lockedUntil) {

  public Account {
    accountId = requiredText(accountId, "accountId");
    username = requiredText(username, "username");
    status = Objects.requireNonNull(status, "status must not be null");
    passwordState = Objects.requireNonNull(passwordState, "passwordState must not be null");
    mfaRequirement = Objects.requireNonNull(mfaRequirement, "mfaRequirement must not be null");
    roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    if (failedLoginCount < 0) {
      throw new IllegalArgumentException("failedLoginCount must not be negative");
    }
  }

  public boolean requiresPasswordChange() {
    return status == AccountStatus.PASSWORD_RESET_REQUIRED || passwordState == PasswordState.RESET_REQUIRED;
  }

  public boolean isLockedAt(OffsetDateTime now) {
    OffsetDateTime currentTime = Objects.requireNonNull(now, "now must not be null");
    if (status != AccountStatus.LOCKED && lockedUntil == null) {
      return false;
    }
    return lockedUntil == null || lockedUntil.isAfter(currentTime);
  }

  private static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
