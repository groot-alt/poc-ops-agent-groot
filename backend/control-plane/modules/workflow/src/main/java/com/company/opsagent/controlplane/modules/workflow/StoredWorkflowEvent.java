package com.company.opsagent.controlplane.modules.workflow;

import java.time.OffsetDateTime;

public record StoredWorkflowEvent(
    String workflowId,
    long sequence,
    String eventId,
    String eventType,
    String eventPayloadJson,
    OffsetDateTime createdAt) {
}
