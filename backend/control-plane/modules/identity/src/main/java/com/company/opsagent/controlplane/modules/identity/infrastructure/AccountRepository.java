package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.Account;
import java.util.Optional;

/**
 * 账号事实源访问接口。
 */
public interface AccountRepository {

  Optional<Account> findByUsername(String username);

  Optional<Account> findByAccountId(String accountId);

  default void save(Account account) {
    throw new UnsupportedOperationException("save is not implemented");
  }
}
