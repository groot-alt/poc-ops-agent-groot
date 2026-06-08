package com.company.opsagent.contracts.workflow;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 将执行请求绑定到准确 Skill 版本和输入输出契约。
 */
public record SkillReference(
    String skillId,
    String version,
    String parameterSchemaId,
    String outputSchemaId) {

  public SkillReference {
    skillId = requiredText(skillId, "skillId");
    version = requiredText(version, "version");
    parameterSchemaId = requiredText(parameterSchemaId, "parameterSchemaId");
    outputSchemaId = requiredText(outputSchemaId, "outputSchemaId");
  }
}
