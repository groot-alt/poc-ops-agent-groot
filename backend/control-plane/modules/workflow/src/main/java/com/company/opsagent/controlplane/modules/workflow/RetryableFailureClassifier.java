package com.company.opsagent.controlplane.modules.workflow;

import com.company.opsagent.contracts.workflow.WorkerExecutionResult;
import com.company.opsagent.contracts.workflow.WorkerExecutionStatus;

public class RetryableFailureClassifier {

  public boolean isRetryable(Throwable error) {
    return false;
  }

  public boolean isRetryable(WorkerExecutionResult result) {
    return result.status() != WorkerExecutionStatus.SUCCEEDED
        && "WORKER_TIMEOUT".equals(result.errorCode());
  }
}
