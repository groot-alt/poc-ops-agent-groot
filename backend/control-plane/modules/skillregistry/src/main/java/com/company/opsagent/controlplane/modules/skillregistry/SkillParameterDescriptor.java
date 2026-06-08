package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.List;

/**
 * Skill 输入参数描述。
 *
 * @param name 参数名，供调用方构造请求和做校验提示
 * @param displayName 便于展示的中文名称
 * @param description 参数语义说明
 * @param type 参数类型
 * @param required 是否必填
 * @param allowedValues 当类型为 ENUM 或需要枚举限制时给出候选值
 * @param defaultValue 可选默认值
 */
public record SkillParameterDescriptor(
    String name,
    String displayName,
    String description,
    SkillParameterType type,
    boolean required,
    List<String> allowedValues,
    String defaultValue) {

  /**
   * 规范化可选集合，避免外部持有可变引用。
   */
  public SkillParameterDescriptor {
    allowedValues = allowedValues == null ? List.of() : List.copyOf(allowedValues);
  }
}
