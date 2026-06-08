package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.skillregistry.SkillCategory;
import com.company.opsagent.controlplane.modules.skillregistry.SkillPublicationStatus;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRiskLevel;
import java.util.List;

/**
 * Skill 路由筛选请求。
 *
 * @param skillId 精确 SkillId，可为空
 * @param category 期望分类，可为空
 * @param maxRiskLevel 最大可接受风险等级，可为空
 * @param requiredParameters 至少必须具备的参数名列表
 * @param requiredTags 至少必须命中的标签列表
 * @param requestContextTags 请求上下文标签，用于匹配灰度发布范围
 * @param publicationStatusRequired 需要的发布状态，可为空
 */
public record SkillRoutingRequest(
    String skillId,
    SkillCategory category,
    SkillRiskLevel maxRiskLevel,
    List<String> requiredParameters,
    List<String> requiredTags,
    List<String> requestContextTags,
    SkillPublicationStatus publicationStatusRequired) {

  /**
   * 防御性复制列表字段。
   */
  public SkillRoutingRequest {
    requiredParameters = requiredParameters == null ? List.of() : List.copyOf(requiredParameters);
    requiredTags = requiredTags == null ? List.of() : List.copyOf(requiredTags);
    requestContextTags = requestContextTags == null ? List.of() : List.copyOf(requestContextTags);
  }
}
