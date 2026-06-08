package com.company.opsagent.executionworker;

import com.company.opsagent.contracts.workflow.ReadOnlyCommandEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * 开发环境节点健康只读适配器。
 *
 * <p>P1 仅返回确定性的开发诊断数据，不访问生产系统，也不执行任意脚本。
 */
public class NodeHealthReadAdapter implements ReadOnlySkillAdapter {

  private final ObjectMapper objectMapper;
  private final Clock clock;

  public NodeHealthReadAdapter(ObjectMapper objectMapper, Clock clock) {
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  public boolean supports(String skillId, String version) {
    return "node-health-read".equals(skillId) && "1.1.0".equals(version);
  }

  @Override
  public JsonNode execute(ReadOnlyCommandEnvelope command) {
    JsonNode nodeNameNode = command.parameters().get("nodeName");
    if (nodeNameNode == null || !nodeNameNode.isTextual() || nodeNameNode.asText().isBlank()) {
      throw new IllegalArgumentException("nodeName is required");
    }

    ObjectNode output = objectMapper.createObjectNode();
    output.put("nodeName", nodeNameNode.asText());
    output.put("status", "HEALTHY");
    output.put("cpuUsagePercent", 18);
    output.put("memoryUsagePercent", 42);
    output.put("diskUsagePercent", 37);
    output.put("lastHeartbeatAt", OffsetDateTime.now(clock).toString());
    return output;
  }
}
