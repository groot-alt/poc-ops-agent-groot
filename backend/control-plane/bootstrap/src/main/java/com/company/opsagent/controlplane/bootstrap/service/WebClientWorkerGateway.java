package com.company.opsagent.controlplane.bootstrap.service;

import com.company.opsagent.contracts.workflow.WorkerExecutionRequest;
import com.company.opsagent.contracts.workflow.WorkerExecutionResult;
import com.company.opsagent.controlplane.modules.workflow.WorkerGateway;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 通过非阻塞 HTTP 调用独立执行 Worker 的控制面适配器。
 */
public class WebClientWorkerGateway implements WorkerGateway {

  private final WebClient webClient;

  public WebClientWorkerGateway(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Mono<WorkerExecutionResult> execute(WorkerExecutionRequest request) {
    return webClient.post()
        .uri("/internal/executions/read-only")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(WorkerExecutionResult.class);
  }
}
