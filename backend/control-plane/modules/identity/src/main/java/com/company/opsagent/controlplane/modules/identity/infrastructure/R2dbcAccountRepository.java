package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.Account;
import com.company.opsagent.controlplane.modules.identity.domain.AccountStatus;
import com.company.opsagent.controlplane.modules.identity.domain.MfaRequirement;
import com.company.opsagent.controlplane.modules.identity.domain.PasswordState;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;

/**
 * 基于 R2DBC 的账号事实源实现。
 */
public class R2dbcAccountRepository implements AccountRepository {

  private final DatabaseClient databaseClient;
  private final Clock clock;

  public R2dbcAccountRepository(DatabaseClient databaseClient, Clock clock) {
    this.databaseClient = databaseClient;
    this.clock = clock;
  }

  @Override
  public Optional<Account> findByUsername(String username) {
    return databaseClient.sql("""
            select account_id,
                   username,
                   account_status,
                   password_state,
                   mfa_requirement,
                   failed_login_count,
                   locked_until
            from identity_account
            where lower(username) = lower(:username)
            """)
        .bind("username", username)
        .map((row, metadata) -> new StoredAccount(
            row.get("account_id", String.class),
            row.get("username", String.class),
            row.get("account_status", String.class),
            row.get("password_state", String.class),
            row.get("mfa_requirement", String.class),
            valueOrZero(row.get("failed_login_count", Integer.class)),
            row.get("locked_until", OffsetDateTime.class)))
        .one()
        .flatMap(this::toAccount)
        .blockOptional();
  }

  @Override
  public Optional<Account> findByAccountId(String accountId) {
    return databaseClient.sql("""
            select account_id,
                   username,
                   account_status,
                   password_state,
                   mfa_requirement,
                   failed_login_count,
                   locked_until
            from identity_account
            where account_id = :accountId
            """)
        .bind("accountId", accountId)
        .map((row, metadata) -> new StoredAccount(
            row.get("account_id", String.class),
            row.get("username", String.class),
            row.get("account_status", String.class),
            row.get("password_state", String.class),
            row.get("mfa_requirement", String.class),
            valueOrZero(row.get("failed_login_count", Integer.class)),
            row.get("locked_until", OffsetDateTime.class)))
        .one()
        .flatMap(this::toAccount)
        .blockOptional();
  }

  @Override
  public void save(Account account) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    GenericExecuteSpec updateSpec = databaseClient.sql("""
            update identity_account
            set username = :username,
                account_status = :accountStatus,
                password_state = :passwordState,
                mfa_requirement = :mfaRequirement,
                failed_login_count = :failedLoginCount,
                locked_until = :lockedUntil,
                updated_at = :updatedAt
            where account_id = :accountId
            """)
        .bind("accountId", account.accountId())
        .bind("username", account.username())
        .bind("accountStatus", account.status().name())
        .bind("passwordState", account.passwordState().name())
        .bind("mfaRequirement", account.mfaRequirement().name())
        .bind("failedLoginCount", account.failedLoginCount())
        .bind("updatedAt", now);
    Long updatedRows = bindNullable(updateSpec, "lockedUntil", account.lockedUntil(), OffsetDateTime.class)
        .fetch()
        .rowsUpdated()
        .block();
    if (updatedRows != null && updatedRows > 0) {
      return;
    }

    GenericExecuteSpec insertSpec = databaseClient.sql("""
            insert into identity_account (
              account_id,
              username,
              account_status,
              password_state,
              mfa_requirement,
              failed_login_count,
              locked_until,
              created_at,
              updated_at
            ) values (
              :accountId,
              :username,
              :accountStatus,
              :passwordState,
              :mfaRequirement,
              :failedLoginCount,
              :lockedUntil,
              :createdAt,
              :updatedAt
            )
            """)
        .bind("accountId", account.accountId())
        .bind("username", account.username())
        .bind("accountStatus", account.status().name())
        .bind("passwordState", account.passwordState().name())
        .bind("mfaRequirement", account.mfaRequirement().name())
        .bind("failedLoginCount", account.failedLoginCount())
        .bind("createdAt", now)
        .bind("updatedAt", now);
    bindNullable(insertSpec, "lockedUntil", account.lockedUntil(), OffsetDateTime.class)
        .fetch()
        .rowsUpdated()
        .block();
  }

  private reactor.core.publisher.Mono<Account> toAccount(StoredAccount account) {
    return loadActiveRoles(account.accountId())
        .map(roleCodes -> new Account(
            account.accountId(),
            account.username(),
            AccountStatus.valueOf(account.accountStatus()),
            PasswordState.valueOf(account.passwordState()),
            MfaRequirement.valueOf(account.mfaRequirement()),
            roleCodes,
            account.failedLoginCount(),
            account.lockedUntil()));
  }

  private reactor.core.publisher.Mono<List<String>> loadActiveRoles(String accountId) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    return databaseClient.sql("""
            select role_code
            from identity_account_role_grant
            where account_id = :accountId
              and revoked_at is null
              and effective_from <= :effectiveAt
              and (effective_to is null or effective_to > :effectiveAt)
            order by role_code asc
            """)
        .bind("accountId", accountId)
        .bind("effectiveAt", now)
        .map((row, metadata) -> row.get("role_code", String.class))
        .all()
        .collectList();
  }

  private GenericExecuteSpec bindNullable(
      GenericExecuteSpec spec,
      String name,
      Object value,
      Class<?> type) {
    return value == null ? spec.bindNull(name, type) : spec.bind(name, value);
  }

  private int valueOrZero(Integer value) {
    return value == null ? 0 : value;
  }

  private record StoredAccount(
      String accountId,
      String username,
      String accountStatus,
      String passwordState,
      String mfaRequirement,
      int failedLoginCount,
      OffsetDateTime lockedUntil) {
  }
}
