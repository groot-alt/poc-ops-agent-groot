package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.skillregistry.RegisteredSkill;

/**
 * 单个 Skill 查询响应。
 *
 * @param skill 当前命中的 Skill 注册记录
 */
public record SkillLookupResponse(RegisteredSkill skill) {
}
