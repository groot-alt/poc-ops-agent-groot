package com.company.opsagent.controlplane.modules.audit;

import java.util.List;
import java.util.Optional;

/**
 * 审计链抽象接口。
 *
 * <p>定义审计事件记录、查询和清理的基础能力，便于在内存实现、文件实现和未来正式存储实现之间切换。
 */
public interface AuditTrail {

  /**
   * 记录一条审计事件。
   */
  void record(AuditEvent event);

  /**
   * 返回当前所有审计事件快照。
   */
  List<AuditEvent> snapshot();

  /**
   * 返回最近一条审计事件。
   */
  Optional<AuditEvent> latest();

  /**
   * 清空当前审计链内容。
   */
  void clear();
}
