package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.List;
import java.util.Optional;

/**
 * Skill 注册中心查询接口。
 *
 * <p>M03 当前先提供只读查询能力，供控制面在真正调度 Skill 前完成目录浏览、
 * 契约回显、发布校验结果查看和候选验证。
 */
public interface SkillRegistryService {

  /**
   * 返回全部已注册 Skill。
   */
  List<RegisteredSkill> listSkills();

  /**
   * 返回某个 Skill 的最新版本记录。
   *
   * @param skillId Skill 稳定标识
   * @return 最新版本的 Skill 记录；如果不存在则返回空
   */
  Optional<RegisteredSkill> findLatest(String skillId);

  /**
   * 返回指定版本的 Skill 记录。
   *
   * @param skillId Skill 稳定标识
   * @param version 需要查询的版本号
   * @return 指定版本的 Skill 记录；如果不存在则返回空
   */
  Optional<RegisteredSkill> findByVersion(String skillId, String version);
}
