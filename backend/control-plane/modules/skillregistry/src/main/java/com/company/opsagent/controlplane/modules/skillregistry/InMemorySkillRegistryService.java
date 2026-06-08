package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 基于内存快照的 Skill 注册中心实现。
 *
 * <p>当前实现适用于控制面启动时一次性加载本地 Skill 清单的场景，优点是简单、
 * 查询稳定，后续如果引入数据库或远端发布中心，只需要替换加载源或实现类。
 */
public class InMemorySkillRegistryService implements SkillRegistryService {

  private final List<RegisteredSkill> registeredSkills;
  private final Map<String, List<RegisteredSkill>> registeredSkillsBySkillId;

  public InMemorySkillRegistryService(List<RegisteredSkill> registeredSkills) {
    this.registeredSkills = registeredSkills.stream()
        .sorted(Comparator.comparing((RegisteredSkill skill) -> skill.descriptor().skillId())
            .thenComparing(skill -> skill.descriptor().version(), Comparator.reverseOrder()))
        .toList();

    LinkedHashMap<String, List<RegisteredSkill>> grouped = new LinkedHashMap<>();
    for (RegisteredSkill registeredSkill : this.registeredSkills) {
      grouped.computeIfAbsent(normalizeSkillId(registeredSkill.descriptor().skillId()), ignored -> new ArrayList<>())
          .add(registeredSkill);
    }
    this.registeredSkillsBySkillId = Map.copyOf(grouped);
  }

  /**
   * 返回注册中心当前全部 Skill 快照。
   */
  @Override
  public List<RegisteredSkill> listSkills() {
    return registeredSkills;
  }

  /**
   * 返回同一个 SkillId 下按当前排序规则的最新版本。
   */
  @Override
  public Optional<RegisteredSkill> findLatest(String skillId) {
    return registeredSkillsBySkillId.getOrDefault(normalizeSkillId(skillId), List.of()).stream().findFirst();
  }

  /**
   * 返回指定版本的 Skill。
   */
  @Override
  public Optional<RegisteredSkill> findByVersion(String skillId, String version) {
    return registeredSkillsBySkillId.getOrDefault(normalizeSkillId(skillId), List.of()).stream()
        .filter(registeredSkill -> registeredSkill.descriptor().version().equals(version))
        .findFirst();
  }

  private String normalizeSkillId(String skillId) {
    return skillId == null ? "" : skillId.toLowerCase(Locale.ROOT);
  }
}
