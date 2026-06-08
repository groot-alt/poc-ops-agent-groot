package com.company.opsagent.executionworker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.company.opsagent.contracts.workflow.OperatorContext;
import com.company.opsagent.contracts.workflow.PolicyDecisionReference;
import com.company.opsagent.contracts.workflow.ReadOnlyCommandEnvelope;
import com.company.opsagent.contracts.workflow.SkillReference;
import com.company.opsagent.contracts.workflow.TraceContext;
import com.company.opsagent.contracts.workflow.WorkerExecutionRequest;
import com.company.opsagent.contracts.workflow.WorkerExecutionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 验证 Worker 只执行允许列表中的只读 Skill，并拒绝过期请求。
 */
class RestrictedReadOnlyExecutionWorkerTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-06-06T15:00:00Z"), ZoneOffset.UTC);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestrictedReadOnlyExecutionWorker worker = new RestrictedReadOnlyExecutionWorker(
      List.of(new NodeHealthReadAdapter(objectMapper, clock)),
      clock);

  @Test
  void executesRegisteredReadOnlySkill() {
    var result = worker.execute(request("node-health-read", "1.1.0", 60));

    assertEquals(WorkerExecutionStatus.SUCCEEDED, result.status());
    assertEquals("HEALTHY", result.output().get("status").asText());
  }

  @Test
  void rejectsExpiredRequest() {
    var result = worker.execute(request("node-health-read", "1.1.0", -1));

    assertEquals(WorkerExecutionStatus.REJECTED, result.status());
    assertEquals("REQUEST_EXPIRED", result.errorCode());
  }

  @Test
  void rejectsUnknownSkillVersion() {
    var result = worker.execute(request("node-health-read", "9.9.9", 60));

    assertEquals(WorkerExecutionStatus.REJECTED, result.status());
    assertEquals("SKILL_NOT_ALLOWED", result.errorCode());
  }

  private WorkerExecutionRequest request(String skillId, String version, long expiresInSeconds) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    OffsetDateTime authorizedAt = expiresInSeconds < 0 ? now.minusMinutes(2) : now;
    OffsetDateTime expiresAt = expiresInSeconds < 0 ? now.minusMinutes(1) : now.plusSeconds(expiresInSeconds);
    var parameters = objectMapper.createObjectNode().put("nodeName", "node-a");
    var command = new ReadOnlyCommandEnvelope(
        "1.0",
        "command-1",
        "workflow-1",
        "idempotency-1",
        "READ_ONLY",
        "development",
        new SkillReference(skillId, version, "node-health-input", "node-health-output"),
        parameters,
        new OperatorContext("operator-1", List.of("ROLE_ops-reader")),
        new PolicyDecisionReference("decision-1", "policy-v1", "ALLOW"),
        new TraceContext("trace-1", "request-1"),
        authorizedAt);
    return new WorkerExecutionRequest("1.0", "execution-1", authorizedAt, expiresAt, command);
  }
}
