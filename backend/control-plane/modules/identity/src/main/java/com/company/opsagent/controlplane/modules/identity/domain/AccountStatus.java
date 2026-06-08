package com.company.opsagent.controlplane.modules.identity.domain;

/**
 * 账号状态。
 */
public enum AccountStatus {
  ACTIVE,
  LOCKED,
  DISABLED,
  PASSWORD_RESET_REQUIRED
}
