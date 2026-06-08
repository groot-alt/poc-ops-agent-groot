package com.company.opsagent.controlplane.bootstrap.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 统一错误响应写出器。
 *
 * <p>在自定义 WebFilter 中无法直接复用 `@RestControllerAdvice`，因此这里提供一套
 * 面向过滤器的结构化错误输出能力。
 */
@Component
public class ApiErrorResponseWriter {

  private final ObjectMapper objectMapper;

  public ApiErrorResponseWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * 向当前响应写出结构化错误体。
   *
   * @param exchange 当前请求交换对象
   * @param status 需要返回的 HTTP 状态码
   * @param code 结构化错误码
   * @param message 返回给调用方的错误说明
   * @return 写出动作完成信号
   */
  public Mono<Void> write(
      ServerWebExchange exchange,
      HttpStatus status,
      String code,
      String message) {
    if (exchange.getResponse().isCommitted()) {
      return Mono.empty();
    }
    ApiError error = new ApiError(
        code,
        message,
        exchange.getRequest().getPath().value(),
        exchange.getRequest().getId(),
        OffsetDateTime.now());
    byte[] body = serialize(error);
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
  }

  /**
   * 序列化错误对象。
   *
   * <p>如果 Jackson 序列化失败，则回退到固定 JSON 字符串，保证调用方仍能收到可解析响应。
   */
  private byte[] serialize(ApiError error) {
    try {
      return objectMapper.writeValueAsBytes(error);
    } catch (JsonProcessingException exception) {
      return ("{\"code\":\"INTERNAL_ERROR\",\"message\":\"serialization failed\"}")
          .getBytes(StandardCharsets.UTF_8);
    }
  }
}
