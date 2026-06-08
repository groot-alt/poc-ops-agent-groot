package com.company.opsagent.controlplane.bootstrap.service;

import com.company.opsagent.controlplane.modules.agentrouting.AgentRoutingModule;
import com.company.opsagent.controlplane.modules.audit.AuditModule;
import com.company.opsagent.controlplane.modules.events.EventsModule;
import com.company.opsagent.controlplane.modules.identity.IdentityModule;
import com.company.opsagent.controlplane.modules.orchestration.OrchestrationModule;
import com.company.opsagent.controlplane.modules.policy.PolicyModule;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRegistryModule;
import com.company.opsagent.controlplane.modules.workflow.WorkflowModule;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 模块目录服务。
 *
 * <p>该服务统一收集设计蓝图中已经落地的模块编号，供健康检查、模块清单和后续运维诊断复用。
 */
@Service
public class ModuleCatalogService {

  /**
   * 返回当前控制面已注册模块编号列表。
   */
  public List<String> moduleIds() {
    return List.of(
        IdentityModule.moduleId(),
        PolicyModule.moduleId(),
        AuditModule.moduleId(),
        SkillRegistryModule.moduleId(),
        AgentRoutingModule.moduleId(),
        WorkflowModule.moduleId(),
        OrchestrationModule.moduleId(),
        EventsModule.moduleId());
  }
}
