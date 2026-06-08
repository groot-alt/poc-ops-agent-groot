package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * M03 Skill 契约与注册中心模块常量定义。
 *
 * <p>当前模块负责承载 Skill Manifest 契约、注册查询接口和本地清单加载能力，
 * 供控制面在不执行具体 Skill 的前提下先完成“声明、校验、查询”的只读闭环。
 */
public final class SkillRegistryModule {

  /**
   * 设计蓝图中为 Skill 契约与注册中心分配的模块编号。
   */
  public static final String MODULE_ID = "M03";

  private SkillRegistryModule() {
  }

  /**
   * 返回模块编号，供模块目录、健康检查和设计追踪复用。
   *
   * @return M03 模块编号
   */
  public static String moduleId() {
    return MODULE_ID;
  }
}
