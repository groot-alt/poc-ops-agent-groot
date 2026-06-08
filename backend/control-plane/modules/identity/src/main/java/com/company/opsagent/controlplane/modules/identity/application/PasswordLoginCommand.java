package com.company.opsagent.controlplane.modules.identity.application;

/**
 * 用户名密码登录命令。
 */
public record PasswordLoginCommand(String username, String password) {

  public PasswordLoginCommand {
    username = requiredText(username, "username");
    password = requiredText(password, "password");
  }

  private static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
