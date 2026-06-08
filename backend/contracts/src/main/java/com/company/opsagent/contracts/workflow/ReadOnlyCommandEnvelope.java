package com.company.opsagent.contracts.workflow;

import static com.company.opsagent.contracts.ContractValues.required;
import static com.company.opsagent.contracts.ContractValues.requiredText;
import static com.company.opsagent.contracts.ContractValues.requiredTime;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

/**
 * 控制面生成的已授权只读命令信封。
 *
 * <p>动态参数必须由 {@code parameterSchemaId} 指向的版本化 Schema 约束。
 */
public record ReadOnlyCommandEnvelope(
    String contractVersion,
    String commandId,
    String workflowId,
    String idempotencyKey,
    String operationClass,
    String targetEnvironment,
    SkillReference skill,
    JsonNode parameters,
    OperatorContext operator,
    PolicyDecisionReference policyDecision,
    TraceContext trace,
    OffsetDateTime requestedAt) {

  public ReadOnlyCommandEnvelope {
    if (!"1.0".equals(contractVersion)) {
      throw new IllegalArgumentException("unsupported command contract version");
    }
    commandId = requiredText(commandId, "commandId");
    workflowId = requiredText(workflowId, "workflowId");
    idempotencyKey = requiredText(idempotencyKey, "idempotencyKey");
    if (!"READ_ONLY".equals(operationClass)) {
      throw new IllegalArgumentException("P1 only accepts READ_ONLY commands");
    }
    targetEnvironment = requiredText(targetEnvironment, "targetEnvironment");
    skill = required(skill, "skill");
    parameters = required(parameters, "parameters");
    if (!parameters.isObject()) {
      throw new IllegalArgumentException("parameters must be a schema-constrained object");
    }
    operator = required(operator, "operator");
    policyDecision = required(policyDecision, "policyDecision");
    trace = required(trace, "trace");
    requestedAt = requiredTime(requestedAt, "requestedAt");
  }
}
