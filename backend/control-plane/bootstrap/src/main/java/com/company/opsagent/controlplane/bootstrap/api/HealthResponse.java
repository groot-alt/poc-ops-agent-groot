package com.company.opsagent.controlplane.bootstrap.api;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 健康检查响应模型。
 *
 * @param status 当前服务状态
 * @param service 服务名称
 * @param version 服务版本
 * @param modules 当前已注册模块列表
 * @param timestamp 响应生成时间
 */
public record HealthResponse(
    String status,
    String service,
    String version,
    List<String> modules,
    OffsetDateTime timestamp) {
}
