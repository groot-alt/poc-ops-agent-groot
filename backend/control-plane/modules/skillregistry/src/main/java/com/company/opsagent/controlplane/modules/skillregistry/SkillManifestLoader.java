package com.company.opsagent.controlplane.modules.skillregistry;

import java.util.List;

/**
 * Skill Manifest 加载器接口。
 *
 * <p>注册中心不关心底层来自文件系统、Git 仓库还是制品仓库，只要求上游能返回
 * 一组已经完成契约校验和发布校验的 Skill 注册记录。
 */
public interface SkillManifestLoader {

  /**
   * 加载全部 Skill Manifest。
   *
   * @return 当前可注册的 Skill 列表
   */
  List<RegisteredSkill> loadAll();
}
