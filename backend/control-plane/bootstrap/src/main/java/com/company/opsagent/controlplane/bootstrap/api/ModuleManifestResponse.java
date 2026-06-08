package com.company.opsagent.controlplane.bootstrap.api;

import java.util.List;

/**
 * 模块清单响应模型。
 *
 * @param service 服务名称
 * @param version 服务版本
 * @param moduleIds 当前控制面注册模块编号列表
 */
public record ModuleManifestResponse(
    String service,
    String version,
    List<String> moduleIds) {
}
