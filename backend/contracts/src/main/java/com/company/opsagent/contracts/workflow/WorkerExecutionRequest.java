package com.company.opsagent.contracts.workflow;

import static com.company.opsagent.contracts.ContractValues.required;
import static com.company.opsagent.contracts.ContractValues.requiredText;
import static com.company.opsagent.contracts.ContractValues.requiredTime;

import java.time.OffsetDateTime;

/**
 * Worker 接收的带过期时间的已授权执行请求。
 */
public record WorkerExecutionRequest(
    String contractVersion,
    String executionRequestId,
    OffsetDateTime authorizedAt,
    OffsetDateTime expiresAt,
    ReadOnlyCommandEnvelope command) {

  public WorkerExecutionRequest {
    if (!"1.0".equals(contractVersion)) {
      throw new IllegalArgumentException("unsupported worker request contract version");
    }
    executionRequestId = requiredText(executionRequestId, "executionRequestId");
    authorizedAt = requiredTime(authorizedAt, "authorizedAt");
    expiresAt = requiredTime(expiresAt, "expiresAt");
    command = required(command, "command");
    if (!expiresAt.isAfter(authorizedAt)) {
      throw new IllegalArgumentException("expiresAt must be after authorizedAt");
    }
  }
}
