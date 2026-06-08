package com.company.opsagent.controlplane.bootstrap.api;

import jakarta.validation.constraints.NotBlank;

/**
 * 显式发布校验请求。
 *
 * @param manifestPath 相对于 Skill 根目录的 Manifest 路径
 */
public record SkillPublicationValidationRequest(
    @NotBlank String manifestPath) {
}
