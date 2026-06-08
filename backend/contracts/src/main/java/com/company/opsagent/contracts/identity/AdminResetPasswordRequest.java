package com.company.opsagent.contracts.identity;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 管理员受控重置密码请求契约。
 */
public record AdminResetPasswordRequest(
    String accountId,
    String reason,
    String temporaryPassword,
    boolean forcePasswordChange) {

  public AdminResetPasswordRequest {
    accountId = requiredText(accountId, "accountId");
    reason = requiredText(reason, "reason");
    temporaryPassword = requiredText(temporaryPassword, "temporaryPassword");
  }
}
