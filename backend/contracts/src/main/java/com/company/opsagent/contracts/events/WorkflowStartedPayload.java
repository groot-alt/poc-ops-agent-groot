package com.company.opsagent.contracts.events;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 工作流开始事件载荷。
 */
public record WorkflowStartedPayload(SemanticEventType payloadType, String commandId, String operatorId)
    implements SemanticEventPayload {

  public WorkflowStartedPayload {
    if (payloadType != SemanticEventType.WORKFLOW_STARTED) {
      throw new IllegalArgumentException("payloadType must be WORKFLOW_STARTED");
    }
    commandId = requiredText(commandId, "commandId");
    operatorId = requiredText(operatorId, "operatorId");
  }
}
