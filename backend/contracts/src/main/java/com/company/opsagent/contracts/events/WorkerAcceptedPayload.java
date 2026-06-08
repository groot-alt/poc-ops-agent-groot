package com.company.opsagent.contracts.events;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * Worker 已接受请求事件载荷。
 */
public record WorkerAcceptedPayload(SemanticEventType payloadType, String executionRequestId)
    implements SemanticEventPayload {

  public WorkerAcceptedPayload {
    if (payloadType != SemanticEventType.WORKER_ACCEPTED) {
      throw new IllegalArgumentException("payloadType must be WORKER_ACCEPTED");
    }
    executionRequestId = requiredText(executionRequestId, "executionRequestId");
  }
}
