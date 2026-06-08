package com.company.opsagent.controlplane.bootstrap.api;

import java.time.OffsetDateTime;

/**
 * 统一 API 错误响应模型。
 *
 * @param code 结构化错误码，供前端和自动化流程识别
 * @param message 面向调用方的错误说明
 * @param path 触发错误的请求路径
 * @param requestId 服务侧请求标识，便于追踪
 * @param timestamp 错误生成时间
 */
public record ApiError(
    String code,
    String message,
    String path,
    String requestId,
    OffsetDateTime timestamp) {
}
