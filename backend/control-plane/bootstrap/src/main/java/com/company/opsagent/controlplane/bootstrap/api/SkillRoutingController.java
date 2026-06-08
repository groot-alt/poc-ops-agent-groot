package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.agentrouting.SkillRoutingCriteria;
import com.company.opsagent.controlplane.modules.agentrouting.SkillRoutingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Skill 路由候选查询接口。
 *
 * <p>该接口把 M03 已注册 Skill 交给 M04 规则路由服务做筛选，
 * 当前阶段先支持 SkillId、分类、风险、参数、标签和发布状态维度。
 */
@RestController
@RequestMapping("/internal/routing/skills")
public class SkillRoutingController {

  private final SkillRoutingService skillRoutingService;

  public SkillRoutingController(SkillRoutingService skillRoutingService) {
    this.skillRoutingService = skillRoutingService;
  }

  /**
   * 返回符合条件的路由候选列表。
   */
  @PostMapping("/search")
  public Mono<SkillRoutingResponse> search(@RequestBody SkillRoutingRequest request) {
    var candidates = skillRoutingService.findCandidates(new SkillRoutingCriteria(
        request.skillId(),
        request.category(),
        request.maxRiskLevel(),
        request.requiredParameters(),
        request.requiredTags(),
        request.requestContextTags(),
        request.publicationStatusRequired()));
    return Mono.just(new SkillRoutingResponse(candidates.size(), candidates));
  }
}
