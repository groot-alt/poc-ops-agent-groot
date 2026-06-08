package com.company.opsagent.controlplane.modules.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.domain.MfaRequirement;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordState;
import com.company.opsagent.controlplane.modules.identity.infrastructure.AccountRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordCredentialRepository;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordVerifier;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * 验证正式身份模块的最小登录用例边界。
 */
class DefaultIdentityAuthenticationServiceTest {

  @Test
  void authenticatesActiveAccount() {
    DefaultIdentityAuthenticationService service = new DefaultIdentityAuthenticationService(
        accountRepository(new Account(
            "account-1",
            "alice",
            AccountStatus.ACTIVE,
            PasswordState.ACTIVE,
            MfaRequirement.NOT_REQUIRED,
            List.of("ROLE_ops-reader"),
            0,
            null)),
        credentialRepository(new PasswordCredential(
            "credential-1",
            "account-1",
            "argon2id",
            "m=65536,t=3,p=2",
            "hash-1",
            1L,
            false)),
        verifier(true));

    IdentityAuthenticationResult result = service.authenticate(new PasswordLoginCommand("alice", "secret"));

    assertEquals("account-1", result.identity().subject());
    assertEquals("alice", result.identity().username());
    assertFalse(result.passwordChangeRequired());
  }

  @Test
  void requiresPasswordChangeForResetAccount() {
    DefaultIdentityAuthenticationService service = new DefaultIdentityAuthenticationService(
        accountRepository(new Account(
            "account-1",
            "alice",
            AccountStatus.PASSWORD_RESET_REQUIRED,
            PasswordState.RESET_REQUIRED,
            MfaRequirement.NOT_REQUIRED,
            List.of("ROLE_ops-reader"),
            0,
            null)),
        credentialRepository(new PasswordCredential(
            "credential-1",
            "account-1",
            "argon2id",
            "m=65536,t=3,p=2",
            "hash-1",
            1L,
            true)),
        verifier(true));

    IdentityAuthenticationResult result = service.authenticate(new PasswordLoginCommand("alice", "temporary-secret"));

    assertTrue(result.passwordChangeRequired());
  }

  @Test
  void rejectsDisabledAccount() {
    DefaultIdentityAuthenticationService service = new DefaultIdentityAuthenticationService(
        accountRepository(new Account(
            "account-1",
            "alice",
            AccountStatus.DISABLED,
            PasswordState.ACTIVE,
            MfaRequirement.NOT_REQUIRED,
            List.of("ROLE_ops-reader"),
            0,
            null)),
        credentialRepository(new PasswordCredential(
            "credential-1",
            "account-1",
            "argon2id",
            "m=65536,t=3,p=2",
            "hash-1",
            1L,
            false)),
        verifier(true));

    IdentityAuthenticationException exception = assertThrows(
        IdentityAuthenticationException.class,
        () -> service.authenticate(new PasswordLoginCommand("alice", "secret")));

    assertEquals("ACCOUNT_DISABLED", exception.errorCode());
  }

  @Test
  void rejectsAccountLockedUntilFuture() {
    DefaultIdentityAuthenticationService service = new DefaultIdentityAuthenticationService(
        accountRepository(new Account(
            "account-1",
            "alice",
            AccountStatus.ACTIVE,
            PasswordState.ACTIVE,
            MfaRequirement.NOT_REQUIRED,
            List.of("ROLE_ops-reader"),
            3,
            OffsetDateTime.now().plusMinutes(10))),
        credentialRepository(new PasswordCredential(
            "credential-1",
            "account-1",
            "argon2id",
            "m=65536,t=3,p=2",
            "hash-1",
            1L,
            false)),
        verifier(true));

    IdentityAuthenticationException exception = assertThrows(
        IdentityAuthenticationException.class,
        () -> service.authenticate(new PasswordLoginCommand("alice", "secret")));

    assertEquals("ACCOUNT_LOCKED", exception.errorCode());
  }

  @Test
  void usesSharedInvalidCredentialsErrorForUnknownAccount() {
    DefaultIdentityAuthenticationService service = new DefaultIdentityAuthenticationService(
        emptyAccountRepository(),
        accountId -> Optional.empty(),
        verifier(true));

    IdentityAuthenticationException exception = assertThrows(
        IdentityAuthenticationException.class,
        () -> service.authenticate(new PasswordLoginCommand("missing", "secret")));

    assertEquals("INVALID_CREDENTIALS", exception.errorCode());
  }

  @Test
  void usesSharedInvalidCredentialsErrorForPasswordMismatch() {
    DefaultIdentityAuthenticationService service = new DefaultIdentityAuthenticationService(
        accountRepository(new Account(
            "account-1",
            "alice",
            AccountStatus.ACTIVE,
            PasswordState.ACTIVE,
            MfaRequirement.NOT_REQUIRED,
            List.of("ROLE_ops-reader"),
            0,
            null)),
        credentialRepository(new PasswordCredential(
            "credential-1",
            "account-1",
            "argon2id",
            "m=65536,t=3,p=2",
            "hash-1",
            1L,
            false)),
        verifier(false));

    IdentityAuthenticationException exception = assertThrows(
        IdentityAuthenticationException.class,
        () -> service.authenticate(new PasswordLoginCommand("alice", "wrong-secret")));

    assertEquals("INVALID_CREDENTIALS", exception.errorCode());
  }

  private AccountRepository accountRepository(Account account) {
    AtomicReference<Account> current = new AtomicReference<>(account);
    return new AccountRepository() {
      @Override
      public Optional<Account> findByUsername(String username) {
        return Optional.of(current.get());
      }

      @Override
      public Optional<Account> findByAccountId(String accountId) {
        return Optional.of(current.get());
      }

      @Override
      public void save(Account account) {
        current.set(account);
      }
    };
  }

  private AccountRepository emptyAccountRepository() {
    return new AccountRepository() {
      @Override
      public Optional<Account> findByUsername(String username) {
        return Optional.empty();
      }

      @Override
      public Optional<Account> findByAccountId(String accountId) {
        return Optional.empty();
      }
    };
  }

  private PasswordCredentialRepository credentialRepository(PasswordCredential credential) {
    return accountId -> Optional.of(credential);
  }

  private PasswordVerifier verifier(boolean matches) {
    return (rawPassword, credential) -> matches;
  }
}
