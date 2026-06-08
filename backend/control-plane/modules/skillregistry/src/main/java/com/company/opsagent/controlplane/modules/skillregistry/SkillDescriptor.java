package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.List;

/**
 * Skill Manifest 的强类型描述对象。
 *
 * @param skillId Skill 稳定标识，供注册中心和路由中心引用
 * @param version Manifest 版本号
 * @param displayName Skill 展示名称
 * @param description Skill 作用说明
 * @param category Skill 分类
 * @param riskLevel Skill 风险等级
 * @param executor 执行器类型
 * @param outputType 输出类型
 * @param readOnly 是否为只读 Skill
 * @param timeoutSeconds 执行超时时间，单位秒
 * @param owner Skill 责任人或责任团队
 * @param requiredRoles 访问该 Skill 需要具备的角色清单
 * @param tags 用于目录检索和过滤的标签
 * @param interceptors 执行前后必须经过的治理拦截器
 * @param parameters 对外暴露的输入参数定义
 */
public record SkillDescriptor(
    String skillId,
    String version,
    String displayName,
    String description,
    SkillCategory category,
    SkillRiskLevel riskLevel,
    SkillExecutorType executor,
    SkillOutputType outputType,
    boolean readOnly,
    int timeoutSeconds,
    String owner,
    List<String> requiredRoles,
    List<String> tags,
    List<SkillInterceptorType> interceptors,
    List<SkillParameterDescriptor> parameters) {

  /**
   * 对集合字段做防御性复制，避免注册后的契约被外部修改。
   */
  public SkillDescriptor {
    requiredRoles = requiredRoles == null ? List.of() : List.copyOf(requiredRoles);
    tags = tags == null ? List.of() : List.copyOf(tags);
    interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
    parameters = parameters == null ? List.of() : List.copyOf(parameters);
  }
}
