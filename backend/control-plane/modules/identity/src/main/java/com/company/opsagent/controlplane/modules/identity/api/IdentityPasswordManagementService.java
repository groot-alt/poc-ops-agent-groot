package com.company.opsagent.controlplane.modules.identity.api;

import com.company.opsagent.controlplane.modules.identity.application.IdentityAuthenticationResult;
import com.company.opsagent.controlplane.modules.identity.application.PasswordChangeCommand;

/**
 * 正式身份模块对外暴露的改密应用接口。
 */
public interface IdentityPasswordManagementService {

  IdentityAuthenticationResult changePassword(String sessionId, PasswordChangeCommand command);
}
