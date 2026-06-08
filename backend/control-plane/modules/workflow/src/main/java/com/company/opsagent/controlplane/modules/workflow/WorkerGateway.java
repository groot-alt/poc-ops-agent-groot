package com.company.opsagent.controlplane.modules.workflow;

import com.company.opsagent.contracts.workflow.WorkerExecutionRequest;
import com.company.opsagent.contracts.workflow.WorkerExecutionResult;
import reactor.core.publisher.Mono;

/**
 * 控制面向独立执行 Worker 提交已授权请求的端口。
 */
public interface WorkerGateway {

  /**
   * 提交只读执行请求，不允许控制面直接执行适配器或脚本。
   */
  Mono<WorkerExecutionResult> execute(WorkerExecutionRequest request);
}
