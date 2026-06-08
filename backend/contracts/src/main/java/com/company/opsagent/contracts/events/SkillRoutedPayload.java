package com.company.opsagent.contracts.events;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * Skill 路由完成事件载荷。
 */
public record SkillRoutedPayload(SemanticEventType payloadType, String skillId, String skillVersion)
    implements SemanticEventPayload {

  public SkillRoutedPayload {
    if (payloadType != SemanticEventType.SKILL_ROUTED) {
      throw new IllegalArgumentException("payloadType must be SKILL_ROUTED");
    }
    skillId = requiredText(skillId, "skillId");
    skillVersion = requiredText(skillVersion, "skillVersion");
  }
}
