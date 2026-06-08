package com.company.opsagent.controlplane.modules.agentrouting;

/**
 * M04 Agent 路由模块常量定义。
 *
 * <p>当前模块负责基于 M03 已注册的 Skill 契约结果做候选筛选和排序，
 * 让控制面能够在不执行 Skill 的前提下先完成“找候选、看约束、做选择”的路由动作。
 */
public final class AgentRoutingModule {

  /**
   * 设计蓝图中为 Agent 路由分配的模块编号。
   */
  public static final String MODULE_ID = "M04";

  private AgentRoutingModule() {
  }

  /**
   * 返回模块编号。
   */
  public static String moduleId() {
    return MODULE_ID;
  }
}
