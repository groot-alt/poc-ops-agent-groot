package com.company.opsagent.controlplane.bootstrap.api;

/**
 * Echo 接口响应模型。
 *
 * @param echo 原样回显的文本内容
 */
public record EchoResponse(String echo) {
}
