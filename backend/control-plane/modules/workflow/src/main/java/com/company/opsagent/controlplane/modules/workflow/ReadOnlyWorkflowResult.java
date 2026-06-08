package com.company.opsagent.controlplane.modules.workflow;

import com.company.opsagent.contracts.events.SemanticEvent;
import com.company.opsagent.contracts.workflow.WorkerExecutionResult;
import java.util.List;

/**
 * 只读诊断工作流的结果和完整语义事件序列。
 */
public record ReadOnlyWorkflowResult(
    String workflowId,
    WorkerExecutionResult executionResult,
    List<SemanticEvent> events) {

  public ReadOnlyWorkflowResult {
    events = List.copyOf(events);
  }
}
