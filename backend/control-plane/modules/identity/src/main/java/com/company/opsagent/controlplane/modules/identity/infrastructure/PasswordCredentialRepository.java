package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
import java.util.Optional;

/**
 * 密码凭据事实源访问接口。
 */
public interface PasswordCredentialRepository {

  Optional<PasswordCredential> findActiveByAccountId(String accountId);

  default void save(PasswordCredential credential) {
    throw new UnsupportedOperationException("save is not implemented");
  }
}
