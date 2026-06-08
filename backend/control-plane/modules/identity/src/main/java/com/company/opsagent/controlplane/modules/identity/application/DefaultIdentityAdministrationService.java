package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.api.IdentityAdministrationService;
import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordState;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountSessionRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordCredentialRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordHasher;
import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * 正式身份模块的管理员受控重置密码服务。
 */
public class DefaultIdentityAdministrationService implements IdentityAdministrationService {

  private final AccountRepository accountRepository;
  private final PasswordCredentialRepository passwordCredentialRepository;
  private final AccountSessionRepository accountSessionRepository;
  private final PasswordHasher passwordHasher;
  private final Clock clock;

  public DefaultIdentityAdministrationService(
      AccountRepository accountRepository,
      PasswordCredentialRepository passwordCredentialRepository,
      AccountSessionRepository accountSessionRepository,
      PasswordHasher passwordHasher,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.passwordCredentialRepository = passwordCredentialRepository;
    this.accountSessionRepository = accountSessionRepository;
    this.passwordHasher = passwordHasher;
    this.clock = clock;
  }

  @Override
  public void resetPassword(AdminResetPasswordCommand command) {
    Account account = accountRepository.findByAccountId(command.accountId())
        .orElseThrow(() -> new IdentityAuthenticationException("ACCOUNT_NOT_FOUND", "account not found"));
    PasswordCredential currentCredential = passwordCredentialRepository.findActiveByAccountId(account.accountId())
        .orElseThrow(() -> new IdentityAuthenticationException("CREDENTIAL_NOT_FOUND", "password credential missing"));
    passwordCredentialRepository.save(passwordHasher.hash(
        account.accountId(),
        command.temporaryPassword(),
        currentCredential.passwordVersion() + 1,
        command.forcePasswordChange()));
    accountRepository.save(new Account(
        account.accountId(),
        account.username(),
        AccountStatus.PASSWORD_RESET_REQUIRED,
        PasswordState.RESET_REQUIRED,
        account.mfaRequirement(),
        account.roleCodes(),
        0,
        null));
    accountSessionRepository.revokeByAccountId(account.accountId(), OffsetDateTime.now(clock), "PASSWORD_RESET");
  }
}
