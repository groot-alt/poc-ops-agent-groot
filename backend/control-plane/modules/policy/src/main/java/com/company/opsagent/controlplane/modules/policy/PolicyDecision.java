package com.company.opsagent.controlplane.modules.policy;

/**
 * 策略决策结果。
 *
 * @param action 本次决策对应的动作标识
 * @param resource 本次决策对应的资源标识
 * @param policyVersion 产生结果的策略版本
 * @param allowed 是否允许执行
 * @param reason 允许或拒绝的说明原因
 */
public record PolicyDecision(
    String action,
    String resource,
    String policyVersion,
    boolean allowed,
    String reason) {
}
