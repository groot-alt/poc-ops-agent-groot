package com.company.opsagent.controlplane.modules.identity.api;

/**
 * 正式身份模块对外暴露的会话管理接口。
 */
public interface IdentitySessionManagementService {

  void logout(String sessionId);
}
