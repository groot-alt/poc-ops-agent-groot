package com.company.opsagent.controlplane.modules.identity.api;

import com.company.opsagent.controlplane.modules.identity.application.AdminResetPasswordCommand;

/**
 * 正式身份模块对外暴露的管理操作应用接口。
 */
public interface IdentityAdministrationService {

  void resetPassword(AdminResetPasswordCommand command);
}
