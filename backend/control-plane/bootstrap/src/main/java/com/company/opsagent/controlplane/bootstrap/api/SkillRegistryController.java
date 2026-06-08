package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.modules.skillregistry.RegisteredSkill;
import com.company.opsagent.controlplane.modules.skillregistry.SkillRegistryService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Skill 注册中心只读查询接口。
 *
 * <p>M03 当前先向控制面提供目录浏览、版本查询和发布校验结果回显能力，
 * 供前端、路由模块和运维人员在真正执行 Skill 之前先确认契约是否存在、
 * 角色是否匹配、输入参数是否完整、是否为正式发布版本。
 */
@Validated
@RestController
@RequestMapping("/internal/skills")
public class SkillRegistryController {

  private final SkillRegistryService skillRegistryService;

  public SkillRegistryController(SkillRegistryService skillRegistryService) {
    this.skillRegistryService = skillRegistryService;
  }

  /**
   * 返回当前注册中心全部 Skill 清单。
   */
  @GetMapping
  public Mono<SkillCatalogResponse> listSkills() {
    List<RegisteredSkill> skills = skillRegistryService.listSkills();
    return Mono.just(new SkillCatalogResponse(skills.size(), skills));
  }

  /**
   * 查询某个 Skill 的最新版本或指定版本。
   *
   * @param skillId Skill 稳定标识
   * @param version 可选版本号；不传时返回最新版本
   */
  @GetMapping("/{skillId}")
  public Mono<SkillLookupResponse> getSkill(
      @PathVariable("skillId") @NotBlank String skillId,
      @RequestParam(value = "version", required = false) String version) {
    RegisteredSkill skill = (version == null || version.isBlank())
        ? skillRegistryService.findLatest(skillId)
            .orElseThrow(() -> new IllegalArgumentException("skill not found: " + skillId))
        : skillRegistryService.findByVersion(skillId, version)
            .orElseThrow(() -> new IllegalArgumentException("skill version not found: " + skillId + ":" + version));
    return Mono.just(new SkillLookupResponse(skill));
  }
}
