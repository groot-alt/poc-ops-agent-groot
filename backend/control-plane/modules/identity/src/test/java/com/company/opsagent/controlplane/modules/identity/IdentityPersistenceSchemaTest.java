package com.company.opsagent.controlplane.modules.identity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * 锁定正式身份模块的数据事实源骨架。
 */
class IdentityPersistenceSchemaTest {

  @Test
  void containsIdentitySchemaMigration() throws IOException {
    Path migration = Path.of("src", "main", "resources", "sql", "migrations", "V001__identity_schema.sql");
    assertTrue(Files.exists(migration));

    String content = Files.readString(migration);
    assertTrue(content.contains("identity_account"));
    assertTrue(content.contains("identity_account_role_grant"));
    assertTrue(content.contains("identity_password_credential"));
    assertTrue(content.contains("identity_account_session"));
    assertTrue(content.contains("identity_password_reset_ticket"));
  }
}
