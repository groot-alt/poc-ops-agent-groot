package com.company.opsagent.controlplane.modules.identity.domain;

import java.util.Objects;

/**
 * 正式身份模块的密码凭据领域对象。
 */
public record PasswordCredential(
    String credentialId,
    String accountId,
    String hashAlgorithm,
    String hashParameters,
    String passwordHash,
    long passwordVersion,
    boolean mustChangeOnNextLogin) {

  public PasswordCredential {
    credentialId = requiredText(credentialId, "credentialId");
    accountId = requiredText(accountId, "accountId");
    hashAlgorithm = requiredText(hashAlgorithm, "hashAlgorithm");
    hashParameters = requiredText(hashParameters, "hashParameters");
    passwordHash = requiredText(passwordHash, "passwordHash");
    if (passwordVersion < 1) {
      throw new IllegalArgumentException("passwordVersion must be positive");
    }
  }

  private static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
