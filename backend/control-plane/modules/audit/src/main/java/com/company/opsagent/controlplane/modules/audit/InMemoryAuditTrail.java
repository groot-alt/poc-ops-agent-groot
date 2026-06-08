package com.company.opsagent.controlplane.modules.audit;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于内存的审计链实现。
 *
 * <p>主要用于单元测试、开发验证和无需持久化的轻量场景。
 */
public class InMemoryAuditTrail implements AuditTrail {

  private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();

  /**
   * 将事件追加到内存列表中。
   */
  @Override
  public void record(AuditEvent event) {
    events.add(event);
  }

  /**
   * 返回内存中的审计快照。
   */
  @Override
  public List<AuditEvent> snapshot() {
    return List.copyOf(events);
  }

  /**
   * 返回最近一条事件。
   */
  @Override
  public Optional<AuditEvent> latest() {
    if (events.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(events.get(events.size() - 1));
  }

  /**
   * 清空内存审计数据。
   */
  @Override
  public void clear() {
    events.clear();
  }
}
