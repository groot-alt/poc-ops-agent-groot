package com.company.opsagent.executionworker;

import com.company.opsagent.contracts.workflow.ReadOnlyCommandEnvelope;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Worker 内显式注册的只读 Skill 适配器边界。
 *
 * <p>该接口不允许接收脚本文本，只能处理已版本化命令信封。
 */
public interface ReadOnlySkillAdapter {

  /**
   * 返回适配器支持的准确 Skill 标识和版本。
   */
  boolean supports(String skillId, String version);

  /**
   * 执行只读诊断并返回受输出 Schema 约束的结果。
   */
  JsonNode execute(ReadOnlyCommandEnvelope command);
}
