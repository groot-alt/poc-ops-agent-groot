package com.company.opsagent.controlplane.modules.identity.application;

/**
 * 登录认证失败时抛出的领域应用错误。
 */
public final class IdentityAuthenticationException extends RuntimeException {

  private final String errorCode;

  public IdentityAuthenticationException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String errorCode() {
    return errorCode;
  }
}
