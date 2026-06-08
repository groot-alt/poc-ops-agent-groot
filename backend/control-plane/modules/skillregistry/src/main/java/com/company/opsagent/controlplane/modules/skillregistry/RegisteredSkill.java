package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * 已通过注册中心校验的 Skill 记录。
 *
 * @param descriptor Skill 契约主体
 * @param publication 发布与签名元数据
 * @param publicationStatus 当前发布状态
 * @param manifestPath 仓库或文件系统中的 Manifest 路径
 */
public record RegisteredSkill(
    SkillDescriptor descriptor,
    SkillPublicationMetadata publication,
    SkillPublicationStatus publicationStatus,
    String manifestPath) {
}
