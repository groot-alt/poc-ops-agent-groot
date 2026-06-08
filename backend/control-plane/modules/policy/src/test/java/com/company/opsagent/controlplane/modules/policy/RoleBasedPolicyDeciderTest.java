package com.company.opsagent.controlplane.modules.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link RoleBasedPolicyDecider} 单元测试。
 *
 * <p>验证基础 RBAC 场景下的允许、拒绝和未知动作处理逻辑。
 */
class RoleBasedPolicyDeciderTest {

  private final RoleBasedPolicyDecider decider = new RoleBasedPolicyDecider(
      "rbac-v1",
      Map.of(
          "internal.health.read", List.of("ROLE_ops-reader", "ROLE_ops-admin"),
          "internal.modules.read", List.of("ROLE_ops-reader", "ROLE_ops-admin"),
          "internal.echo.read", List.of("ROLE_ops-reader", "ROLE_ops-admin"),
          "internal.failures.read", List.of("ROLE_ops-admin"),
          "internal.audit.read", List.of("ROLE_ops-admin", "ROLE_ops-auditor")));

  @Test
  void allowsReaderToReadHealth() {
    // reader 角色应当具备健康检查读取权限。
    OperatorIdentity operator = new OperatorIdentity("u-1", "alice", List.of("ROLE_ops-reader"));

    PolicyDecision decision = decider.decide(operator, "internal.health.read", "/internal/healthz");

    assertTrue(decision.allowed());
  }

  @Test
  void deniesReaderFromFailureProbe() {
    // 异常探针接口要求 admin 权限，reader 角色应被拒绝。
    OperatorIdentity operator = new OperatorIdentity("u-2", "bob", List.of("ROLE_ops-reader"));

    PolicyDecision decision = decider.decide(operator, "internal.failures.read", "/internal/failures/illegal-argument");

    assertFalse(decision.allowed());
  }

  @Test
  void deniesUnknownAction() {
    // 未配置的动作不能默认放行，避免策略缺口。
    OperatorIdentity operator = new OperatorIdentity("u-3", "carol", List.of("ROLE_ops-admin"));

    PolicyDecision decision = decider.decide(operator, "internal.unknown", "/internal/unknown");

    assertFalse(decision.allowed());
  }
}
