package com.company.opsagent.contracts.identity;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 身份相关结构化错误响应契约。
 */
public record IdentityErrorResponse(
    String errorCode,
    String message,
    String traceId) {

  public IdentityErrorResponse {
    errorCode = requiredText(errorCode, "errorCode");
    message = requiredText(message, "message");
    traceId = requiredText(traceId, "traceId");
  }
}
