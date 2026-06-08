package com.company.opsagent.controlplane.modules.identity.application;

/**
 * 管理员受控重置密码命令。
 */
public record AdminResetPasswordCommand(
    String accountId,
    String reason,
    String temporaryPassword,
    boolean forcePasswordChange) {

  public AdminResetPasswordCommand {
    accountId = requiredText(accountId, "accountId");
    reason = requiredText(reason, "reason");
    temporaryPassword = requiredText(temporaryPassword, "temporaryPassword");
  }

  private static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
