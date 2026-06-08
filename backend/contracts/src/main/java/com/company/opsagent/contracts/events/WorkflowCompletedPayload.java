package com.company.opsagent.contracts.events;

import static com.company.opsagent.contracts.ContractValues.required;
import static com.company.opsagent.contracts.ContractValues.requiredText;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 工作流成功完成事件载荷。
 */
public record WorkflowCompletedPayload(
    SemanticEventType payloadType,
    String outputSchemaId,
    JsonNode output) implements SemanticEventPayload {

  public WorkflowCompletedPayload {
    if (payloadType != SemanticEventType.WORKFLOW_COMPLETED) {
      throw new IllegalArgumentException("payloadType must be WORKFLOW_COMPLETED");
    }
    outputSchemaId = requiredText(outputSchemaId, "outputSchemaId");
    output = required(output, "output");
  }
}
