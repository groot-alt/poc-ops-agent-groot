package com.company.opsagent.controlplane.modules.identity.api;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import com.company.opsagent.controlplane.modules.identity.application.IdentityAuthenticationResult;
import com.company.opsagent.controlplane.modules.identity.application.PasswordLoginCommand;

/**
 * 正式身份模块对外暴露的登录认证应用接口。
 */
public interface IdentityAuthenticationService {

  IdentityAuthenticationResult authenticate(PasswordLoginCommand command);
}
