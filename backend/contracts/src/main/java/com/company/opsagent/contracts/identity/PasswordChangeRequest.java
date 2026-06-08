package com.company.opsagent.contracts.identity;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 首次登录改密或主动修改密码请求契约。
 */
public record PasswordChangeRequest(String currentPassword, String newPassword) {

  public PasswordChangeRequest {
    currentPassword = requiredText(currentPassword, "currentPassword");
    newPassword = requiredText(newPassword, "newPassword");
    if (currentPassword.equals(newPassword)) {
      throw new IllegalArgumentException("newPassword must differ from currentPassword");
    }
  }
}
