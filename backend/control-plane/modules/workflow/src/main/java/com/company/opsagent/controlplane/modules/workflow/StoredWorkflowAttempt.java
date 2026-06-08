package com.company.opsagent.controlplane.modules.workflow;

import java.time.OffsetDateTime;

public record StoredWorkflowAttempt(
    String workflowId,
    int attemptNo,
    String executionRequestId,
    StoredWorkflowAttemptKind attemptKind,
    StoredWorkflowStatus status,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    OffsetDateTime expiresAt,
    String workerErrorCode,
    String workerErrorMessage,
    boolean retryable) {
}
