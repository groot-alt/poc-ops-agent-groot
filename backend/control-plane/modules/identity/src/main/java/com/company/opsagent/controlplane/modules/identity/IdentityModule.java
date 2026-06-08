package com.company.opsagent.controlplane.modules.identity;

/**
 * 身份模块元信息定义。
 *
 * <p>当前仅用于声明设计蓝图中的模块编号，供健康检查和模块清单统一引用。
 */
public final class IdentityModule {

  private IdentityModule() {
  }

  /**
   * 返回身份模块编号。
   */
  public static String moduleId() {
    return "M01";
  }
}
