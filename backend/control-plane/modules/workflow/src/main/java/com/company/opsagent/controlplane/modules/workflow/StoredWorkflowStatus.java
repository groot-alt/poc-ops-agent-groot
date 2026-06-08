package com.company.opsagent.controlplane.modules.workflow;

public enum StoredWorkflowStatus {
  PENDING,
  ROUTED,
  DISPATCHING,
  RUNNING,
  SUCCEEDED,
  FAILED_RETRYABLE,
  FAILED_TERMINAL,
  REPLAYING
}
