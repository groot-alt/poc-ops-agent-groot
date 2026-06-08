package com.company.opsagent.contracts.events;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 工作流失败事件载荷。
 */
public record WorkflowFailedPayload(SemanticEventType payloadType, String errorCode, String message)
    implements SemanticEventPayload {

  public WorkflowFailedPayload {
    if (payloadType != SemanticEventType.WORKFLOW_FAILED) {
      throw new IllegalArgumentException("payloadType must be WORKFLOW_FAILED");
    }
    errorCode = requiredText(errorCode, "errorCode");
    message = requiredText(message, "message");
  }
}
