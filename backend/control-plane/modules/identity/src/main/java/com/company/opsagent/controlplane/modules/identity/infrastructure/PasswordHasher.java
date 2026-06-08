package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;

/**
 * 密码哈希器。
 */
public interface PasswordHasher {

  PasswordCredential hash(
      String accountId,
      String rawPassword,
      long passwordVersion,
      boolean mustChangeOnNextLogin);
}
