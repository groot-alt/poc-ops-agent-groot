package com.company.opsagent.contracts.workflow;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 跨控制面和 Worker 传播的链路追踪上下文。
 */
public record TraceContext(String traceId, String requestId) {

  public TraceContext {
    traceId = requiredText(traceId, "traceId");
    requestId = requiredText(requestId, "requestId");
  }
}
