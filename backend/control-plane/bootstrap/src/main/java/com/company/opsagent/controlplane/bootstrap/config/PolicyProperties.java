package com.company.opsagent.controlplane.bootstrap.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 策略配置属性。
 *
 * <p>当前使用最简单的“动作 -> 角色列表”方式承载 RBAC 规则，后续如果切到独立策略源，
 * 这个对象仍可作为配置装配层的输入模型。
 */
@ConfigurationProperties(prefix = "ops-agent.policy")
public class PolicyProperties {

  private String version = "rbac-v1";
  private Map<String, List<String>> requiredRolesByAction = new LinkedHashMap<>();

  /**
   * 返回当前策略版本号。
   */
  public String getVersion() {
    return version;
  }

  /**
   * 设置当前策略版本号。
   *
   * @param version 用于审计和策略决策结果标识的版本号
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * 返回动作到角色要求的映射表。
   */
  public Map<String, List<String>> getRequiredRolesByAction() {
    return requiredRolesByAction;
  }

  /**
   * 设置动作到角色要求的映射表。
   *
   * @param requiredRolesByAction 外部配置驱动的 RBAC 规则定义
   */
  public void setRequiredRolesByAction(Map<String, List<String>> requiredRolesByAction) {
    this.requiredRolesByAction = requiredRolesByAction;
  }
}
