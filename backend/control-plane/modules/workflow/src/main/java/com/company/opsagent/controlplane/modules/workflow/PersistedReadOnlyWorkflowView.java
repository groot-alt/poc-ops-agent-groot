package com.company.opsagent.controlplane.modules.workflow;

import com.company.opsagent.contracts.events.SemanticEvent;
import com.company.opsagent.contracts.workflow.ReadOnlyCommandEnvelope;
import com.company.opsagent.contracts.workflow.WorkerExecutionResult;
import java.util.List;

public record PersistedReadOnlyWorkflowView(
    StoredReadOnlyWorkflow workflow,
    ReadOnlyCommandEnvelope command,
    WorkerExecutionResult executionResult,
    List<StoredWorkflowAttempt> attempts,
    List<SemanticEvent> events) {

  public PersistedReadOnlyWorkflowView {
    attempts = List.copyOf(attempts);
    events = List.copyOf(events);
  }

  public ReadOnlyWorkflowResult toWorkflowResult() {
    if (executionResult == null) {
      throw new IllegalStateException("executionResult is not available for workflow " + workflow.workflowId());
    }
    return new ReadOnlyWorkflowResult(
        workflow.workflowId(),
        executionResult,
        events);
  }
}
