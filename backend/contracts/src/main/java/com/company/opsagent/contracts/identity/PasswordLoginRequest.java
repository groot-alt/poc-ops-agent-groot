package com.company.opsagent.contracts.identity;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 浏览器用户名密码登录请求契约。
 */
public record PasswordLoginRequest(String username, String password) {

  public PasswordLoginRequest {
    username = requiredText(username, "username");
    password = requiredText(password, "password");
  }
}
