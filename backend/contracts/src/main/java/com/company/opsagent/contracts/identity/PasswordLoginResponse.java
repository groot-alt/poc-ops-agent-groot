package com.company.opsagent.contracts.identity;

import static com.company.opsagent.contracts.ContractValues.requiredText;

import java.util.List;

/**
 * 浏览器用户名密码登录响应契约。
 */
public record PasswordLoginResponse(
    boolean authenticated,
    String subject,
    String username,
    List<String> roles,
    boolean passwordChangeRequired) {

  public PasswordLoginResponse {
    roles = roles == null ? List.of() : List.copyOf(roles);
    if (authenticated) {
      subject = requiredText(subject, "subject");
      username = requiredText(username, "username");
    } else if (subject != null || username != null || !roles.isEmpty()) {
      throw new IllegalArgumentException("unauthenticated login response must not contain principal details");
    }
  }
}
