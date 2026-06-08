package com.company.opsagent.controlplane.modules.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.company.opsagent.contracts.events.SemanticEventType;
import com.company.opsagent.contracts.workflow.OperatorContext;
import com.company.opsagent.contracts.workflow.PolicyDecisionReference;
import com.company.opsagent.contracts.workflow.ReadOnlyCommandEnvelope;
import com.company.opsagent.contracts.workflow.SkillReference;
import com.company.opsagent.contracts.workflow.TraceContext;
import com.company.opsagent.contracts.workflow.WorkerExecutionResult;
import com.company.opsagent.contracts.workflow.WorkerExecutionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReadOnlyWorkflowRecoveryServiceTest {

  @Test
  void replaysRetryableWorkflowExactlyOnce() {
    Clock clock = Clock.fixed(Instant.parse("2026-06-06T15:00:00Z"), ZoneOffset.UTC);
    ObjectMapper objectMapper = new ObjectMapper();
    var store = new InMemoryReadOnlyWorkflowStoreFixture();
    var command = new ReadOnlyCommandEnvelope(
        "1.0",
        "command-1",
        "workflow-1",
        "idempotency-1",
        "READ_ONLY",
        "development",
        new SkillReference(
            "node-health-read",
            "1.1.0",
            "node-health-read:1.1.0:input",
            "node-health-read:1.1.0:output"),
        objectMapper.createObjectNode().put("nodeName", "node-a"),
        new OperatorContext("operator-1", List.of("ROLE_ops-reader")),
        new PolicyDecisionReference("decision-1", "policy-v1", "ALLOW"),
        new TraceContext("trace-1", "request-1"),
        OffsetDateTime.now(clock).minusMinutes(2));
    store.seedRetryableFailure(
        "workflow-1",
        "idempotency-1",
        command,
        new WorkerExecutionResult(
            "1.0",
            "execution-1",
            "command-1",
            "workflow-1",
            WorkerExecutionStatus.FAILED,
            "node-health-read:1.1.0:output",
            objectMapper.nullNode(),
            "WORKER_TIMEOUT",
            "worker execution timed out",
            OffsetDateTime.now(clock).minusMinutes(2)),
        0,
        1);
    var recoveryService = new ReadOnlyWorkflowRecoveryService(
        store,
        request -> Mono.just(new WorkerExecutionResult(
            "1.0",
            request.executionRequestId(),
            request.command().commandId(),
            request.command().workflowId(),
            WorkerExecutionStatus.SUCCEEDED,
            request.command().skill().outputSchemaId(),
            objectMapper.createObjectNode().put("status", "HEALTHY"),
            null,
            null,
            OffsetDateTime.now(clock))),
        clock,
        new RetryableFailureClassifier());

    StepVerifier.create(recoveryService.recoverStaleWorkflows())
        .expectNext(1)
        .verifyComplete();

    assertEquals(StoredWorkflowStatus.SUCCEEDED, store.workflowById("workflow-1").status());
    assertEquals(1, store.workflowById("workflow-1").replayCount());
    assertEquals(SemanticEventType.WORKFLOW_COMPLETED, store.eventsByWorkflowId("workflow-1").getLast().type());
  }

  @Test
  void replaysExpiredRunningWorkflowExactlyOnce() {
    Clock clock = Clock.fixed(Instant.parse("2026-06-06T15:00:00Z"), ZoneOffset.UTC);
    ObjectMapper objectMapper = new ObjectMapper();
    var store = new InMemoryReadOnlyWorkflowStoreFixture();
    var command = new ReadOnlyCommandEnvelope(
        "1.0",
        "command-2",
        "workflow-2",
        "idempotency-2",
        "READ_ONLY",
        "development",
        new SkillReference(
            "node-health-read",
            "1.1.0",
            "node-health-read:1.1.0:input",
            "node-health-read:1.1.0:output"),
        objectMapper.createObjectNode().put("nodeName", "node-b"),
        new OperatorContext("operator-1", List.of("ROLE_ops-reader")),
        new PolicyDecisionReference("decision-1", "policy-v1", "ALLOW"),
        new TraceContext("trace-1", "request-1"),
        OffsetDateTime.now(clock).minusMinutes(2));
    store.seedRunningWorkflow(
        "workflow-2",
        "idempotency-2",
        command,
        "execution-2",
        OffsetDateTime.now(clock).minusMinutes(2),
        OffsetDateTime.now(clock).minusMinutes(1));
    var recoveryService = new ReadOnlyWorkflowRecoveryService(
        store,
        request -> Mono.just(new WorkerExecutionResult(
            "1.0",
            request.executionRequestId(),
            request.command().commandId(),
            request.command().workflowId(),
            WorkerExecutionStatus.SUCCEEDED,
            request.command().skill().outputSchemaId(),
            objectMapper.createObjectNode().put("status", "HEALTHY"),
            null,
            null,
            OffsetDateTime.now(clock))),
        clock,
        new RetryableFailureClassifier());

    StepVerifier.create(recoveryService.recoverStaleWorkflows())
        .expectNext(1)
        .verifyComplete();

    assertEquals(StoredWorkflowStatus.SUCCEEDED, store.workflowById("workflow-2").status());
    assertEquals(1, store.workflowById("workflow-2").replayCount());
  }

  @Test
  void doesNotReplayFreshRunningWorkflowBeforeExpiry() {
    Clock clock = Clock.fixed(Instant.parse("2026-06-06T15:00:00Z"), ZoneOffset.UTC);
    ObjectMapper objectMapper = new ObjectMapper();
    var store = new InMemoryReadOnlyWorkflowStoreFixture();
    var command = new ReadOnlyCommandEnvelope(
        "1.0",
        "command-3",
        "workflow-3",
        "idempotency-3",
        "READ_ONLY",
        "development",
        new SkillReference(
            "node-health-read",
            "1.1.0",
            "node-health-read:1.1.0:input",
            "node-health-read:1.1.0:output"),
        objectMapper.createObjectNode().put("nodeName", "node-c"),
        new OperatorContext("operator-1", List.of("ROLE_ops-reader")),
        new PolicyDecisionReference("decision-1", "policy-v1", "ALLOW"),
        new TraceContext("trace-1", "request-1"),
        OffsetDateTime.now(clock).minusSeconds(5));
    store.seedRunningWorkflow(
        "workflow-3",
        "idempotency-3",
        command,
        "execution-3",
        OffsetDateTime.now(clock).minusSeconds(5),
        OffsetDateTime.now(clock).plusSeconds(25));
    var recoveryService = new ReadOnlyWorkflowRecoveryService(
        store,
        request -> Mono.error(new AssertionError("worker gateway should not be invoked")),
        clock,
        new RetryableFailureClassifier());

    StepVerifier.create(recoveryService.recoverStaleWorkflows())
        .expectNext(0)
        .verifyComplete();
  }
}
