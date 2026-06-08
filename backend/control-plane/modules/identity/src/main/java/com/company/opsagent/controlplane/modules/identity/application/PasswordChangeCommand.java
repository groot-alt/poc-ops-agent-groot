package com.company.opsagent.controlplane.modules.identity.application;

/**
 * 改密命令。
 */
public record PasswordChangeCommand(String currentPassword, String newPassword) {

  public PasswordChangeCommand {
    currentPassword = requiredText(currentPassword, "currentPassword");
    newPassword = requiredText(newPassword, "newPassword");
  }

  private static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
