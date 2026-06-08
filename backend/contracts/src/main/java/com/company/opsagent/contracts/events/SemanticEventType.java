package com.company.opsagent.contracts.events;

/**
 * P1 只读工作流对操作台公开的语义事件类型。
 */
public enum SemanticEventType {
  WORKFLOW_STARTED,
  SKILL_ROUTED,
  WORKER_ACCEPTED,
  WORKFLOW_COMPLETED,
  WORKFLOW_FAILED
}
