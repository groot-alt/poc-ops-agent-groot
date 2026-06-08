package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.skillregistry.SkillPublicationValidationResult;

/**
 * 显式发布校验响应。
 *
 * @param result 发布校验结果
 */
public record SkillPublicationValidationResponse(
    SkillPublicationValidationResult result) {
}
