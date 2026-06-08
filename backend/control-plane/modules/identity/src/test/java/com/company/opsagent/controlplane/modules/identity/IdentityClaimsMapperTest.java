package com.company.opsagent.controlplane.modules.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link IdentityClaimsMapper} 单元测试。
 *
 * <p>验证身份映射过程中的用户名回退、角色标准化和必填字段校验。
 */
class IdentityClaimsMapperTest {

  private final IdentityClaimsMapper mapper = new IdentityClaimsMapper();

  @Test
  void normalizesUsernameAndRoles() {
    // 验证角色会被补齐 ROLE_ 前缀并去重，用户名保持原值。
    OperatorIdentity identity = mapper.fromClaims(
        "user-01",
        "alice",
        List.of("ops-reader", "ROLE_ops-admin", "ops-reader"));

    assertEquals("user-01", identity.subject());
    assertEquals("alice", identity.username());
    assertEquals(List.of("ROLE_ops-reader", "ROLE_ops-admin"), identity.roles());
  }

  @Test
  void fallsBackToSubjectWhenUsernameMissing() {
    // 当用户名 claim 为空白时，应回退到 subject，确保审计主体可追踪。
    OperatorIdentity identity = mapper.fromClaims("user-02", " ", List.of());

    assertEquals("user-02", identity.username());
  }

  @Test
  void rejectsBlankSubject() {
    // subject 是系统内部身份模型的硬约束，不能为空白。
    assertThrows(IllegalArgumentException.class, () -> mapper.fromClaims(" ", "alice", List.of()));
  }
}
