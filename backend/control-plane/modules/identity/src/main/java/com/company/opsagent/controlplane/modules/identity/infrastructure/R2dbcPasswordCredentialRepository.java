package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * 基于 R2DBC 的密码凭据事实源实现。
 */
public class R2dbcPasswordCredentialRepository implements PasswordCredentialRepository {

  private final DatabaseClient databaseClient;
  private final Clock clock;

  public R2dbcPasswordCredentialRepository(DatabaseClient databaseClient, Clock clock) {
    this.databaseClient = databaseClient;
    this.clock = clock;
  }

  @Override
  public Optional<PasswordCredential> findActiveByAccountId(String accountId) {
    return databaseClient.sql("""
            select credential_id,
                   account_id,
                   hash_algorithm,
                   hash_parameters,
                   password_hash,
                   password_version,
                   must_change_on_next_login
            from identity_password_credential
            where account_id = :accountId
              and rotated_at is null
            order by password_version desc
            limit 1
            """)
        .bind("accountId", accountId)
        .map((row, metadata) -> new PasswordCredential(
            row.get("credential_id", String.class),
            row.get("account_id", String.class),
            row.get("hash_algorithm", String.class),
            row.get("hash_parameters", String.class),
            row.get("password_hash", String.class),
            valueOrZero(row.get("password_version", Long.class)),
            Boolean.TRUE.equals(row.get("must_change_on_next_login", Boolean.class))))
        .one()
        .blockOptional();
  }

  @Override
  public void save(PasswordCredential credential) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    databaseClient.sql("""
            update identity_password_credential
            set rotated_at = :rotatedAt
            where account_id = :accountId
              and rotated_at is null
              and credential_id <> :credentialId
            """)
        .bind("rotatedAt", now)
        .bind("accountId", credential.accountId())
        .bind("credentialId", credential.credentialId())
        .fetch()
        .rowsUpdated()
        .block();

    Long updatedRows = databaseClient.sql("""
            update identity_password_credential
            set hash_algorithm = :hashAlgorithm,
                hash_parameters = :hashParameters,
                password_hash = :passwordHash,
                password_version = :passwordVersion,
                must_change_on_next_login = :mustChangeOnNextLogin,
                rotated_at = null
            where credential_id = :credentialId
            """)
        .bind("credentialId", credential.credentialId())
        .bind("hashAlgorithm", credential.hashAlgorithm())
        .bind("hashParameters", credential.hashParameters())
        .bind("passwordHash", credential.passwordHash())
        .bind("passwordVersion", credential.passwordVersion())
        .bind("mustChangeOnNextLogin", credential.mustChangeOnNextLogin())
        .fetch()
        .rowsUpdated()
        .block();
    if (updatedRows != null && updatedRows > 0) {
      return;
    }

    databaseClient.sql("""
            insert into identity_password_credential (
              credential_id,
              account_id,
              hash_algorithm,
              hash_parameters,
              password_hash,
              password_version,
              must_change_on_next_login,
              created_at,
              rotated_at
            ) values (
              :credentialId,
              :accountId,
              :hashAlgorithm,
              :hashParameters,
              :passwordHash,
              :passwordVersion,
              :mustChangeOnNextLogin,
              :createdAt,
              null
            )
            """)
        .bind("credentialId", credential.credentialId())
        .bind("accountId", credential.accountId())
        .bind("hashAlgorithm", credential.hashAlgorithm())
        .bind("hashParameters", credential.hashParameters())
        .bind("passwordHash", credential.passwordHash())
        .bind("passwordVersion", credential.passwordVersion())
        .bind("mustChangeOnNextLogin", credential.mustChangeOnNextLogin())
        .bind("createdAt", now)
        .fetch()
        .rowsUpdated()
        .block();
  }

  private long valueOrZero(Long value) {
    return value == null ? 0L : value;
  }
}
