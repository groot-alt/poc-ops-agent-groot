package com.company.opsagent.controlplane.bootstrap.security;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import reactor.core.publisher.Mono;

/**
 * 操作人身份认证接口。
 *
 * <p>输入原始 Bearer Token，输出经过校验和标准化后的操作人身份对象。
 */
public interface OperatorIdentityAuthenticator {

  /**
   * 校验令牌并解析为操作人身份。
   *
   * @param token 原始 Bearer Token
   * @return 认证成功时返回身份信息，失败时返回空流或异常
   */
  Mono<OperatorIdentity> authenticate(String token);
}
