package com.company.opsagent.controlplane.modules.skillregistry;

import java.time.OffsetDateTime;

/**
 * Skill 发布元数据。
 *
 * @param publishedBy 发布人或发布流水责任主体
 * @param publishedAt 发布时间
 * @param checksumSha256 `manifest.json` 文件原始字节的 SHA-256 摘要
 * @param signatureAlgorithm 当前发布签名算法
 * @param signature 基于摘要计算得到的签名值
 */
public record SkillPublicationMetadata(
    String publishedBy,
    OffsetDateTime publishedAt,
    String checksumSha256,
    String signatureAlgorithm,
    String signature) {
}
