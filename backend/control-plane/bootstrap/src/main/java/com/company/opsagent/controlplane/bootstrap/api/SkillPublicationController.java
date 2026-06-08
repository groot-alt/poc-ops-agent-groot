package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.skillregistry.SkillPublicationWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Skill 显式发布校验接口。
 *
 * <p>P1 阶段该接口只负责“校验并回显结果”，不直接写注册中心，
 * 让发布动作先变成可审计、可回放的显式控制面行为。
 */
@RestController
@RequestMapping("/internal/skills/publications")
public class SkillPublicationController {

  private final SkillPublicationWorkflowService publicationWorkflowService;

  public SkillPublicationController(SkillPublicationWorkflowService publicationWorkflowService) {
    this.publicationWorkflowService = publicationWorkflowService;
  }

  /**
   * 执行一次显式发布校验动作。
   */
  @PostMapping("/validate")
  public Mono<SkillPublicationValidationResponse> validate(
      @Valid @RequestBody SkillPublicationValidationRequest request) {
    return Mono.just(new SkillPublicationValidationResponse(
        publicationWorkflowService.validatePublication(request.manifestPath())));
  }
}
