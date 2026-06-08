package com.company.opsagent.executionworker;

import com.company.opsagent.contracts.workflow.OperatorContext;
import com.company.opsagent.contracts.workflow.PolicyDecisionReference;
import com.company.opsagent.contracts.workflow.ReadOnlyCommandEnvelope;
import com.company.opsagent.contracts.workflow.SkillReference;
import com.company.opsagent.contracts.workflow.TraceContext;
import com.company.opsagent.contracts.workflow.WorkerExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 验证独立 Worker HTTP 边界仅接受版本化只读执行请求。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkerExecutionControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void executesReadOnlyRequestThroughHttpBoundary() {
    OffsetDateTime now = OffsetDateTime.now();
    var command = new ReadOnlyCommandEnvelope(
        "1.0",
        "command-http",
        "workflow-http",
        "idempotency-http",
        "READ_ONLY",
        "development",
        new SkillReference("node-health-read", "1.1.0", "node-health-input", "node-health-output"),
        objectMapper.createObjectNode().put("nodeName", "node-http"),
        new OperatorContext("operator-http", List.of("ROLE_ops-reader")),
        new PolicyDecisionReference("decision-http", "policy-v1", "ALLOW"),
        new TraceContext("trace-http", "request-http"),
        now);
    var request = new WorkerExecutionRequest("1.0", "execution-http", now, now.plusSeconds(30), command);

    WebTestClient.bindToServer()
        .baseUrl("http://127.0.0.1:" + port)
        .build()
        .post()
        .uri("/internal/executions/read-only")
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.status").isEqualTo("SUCCEEDED")
        .jsonPath("$.output.nodeName").isEqualTo("node-http");
  }
}
